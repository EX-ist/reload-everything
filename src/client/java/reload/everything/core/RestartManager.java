package reload.everything.core;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.EventLoopGroupHolder;
import reload.everything.ReloadEverythingPreLaunch;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.client.multiplayer.ServerData;
import java.lang.reflect.Field;

import java.net.InetSocketAddress;
import java.time.Duration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class RestartManager {

    private static Runnable clientReloadCallback = null;

    public static void setClientReloadCallback(Runnable callback) {
        clientReloadCallback = callback;
    }

    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    // ==================== 单人游戏存档重载 ====================
    public static void reloadWorld() {
        Minecraft client = Minecraft.getInstance();
        if (!client.isLocalServer()) {
            System.out.println("[Reload Everything] 当前不在单机存档中");
            return;
        }

        IntegratedServer server = client.getSingleplayerServer();
        if (server == null) {
            System.out.println("[Reload Everything] 无法获取服务器实例");
            return;
        }

        String worldName = server.getWorldData().getLevelName();
        System.out.println("[Reload Everything] 正在重新加载存档: " + worldName);

        client.disconnectFromWorld(Component.literal("正在重新加载存档..."));

        client.createWorldOpenFlows().openWorld(worldName, () -> {
            System.out.println("[Reload Everything] 存档已重新加载: " + worldName);
        });
    }
    public static void reconnectServer() {
    Minecraft client = Minecraft.getInstance();
    ServerData serverData = client.getCurrentServer();
    if (serverData == null) {
        System.out.println("[Reload Everything] 当前不在多人服务器中");
        return;
    }

    String address = serverData.ip;
    System.out.println("[Reload Everything] 正在重新连接: " + address);

    String[] parts = address.split(":");
    String host = parts[0];
    int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 25565;

    client.disconnectFromWorld(Component.literal("正在重新连接..."));

    InetSocketAddress socketAddress = new InetSocketAddress(host, port);
    EventLoopGroupHolder eventLoopGroup = EventLoopGroupHolder.remote(client.options.useNativeTransport());
    Connection connection = Connection.connectToServer(socketAddress, eventLoopGroup, null);

    ClientHandshakePacketListenerImpl handshake = new ClientHandshakePacketListenerImpl(
        connection,
        client,
        serverData,
        null,
        false,
        Duration.ZERO,
        (status) -> System.out.println("[Reload Everything] 连接状态: " + status.getString()),
        new LevelLoadTracker(0L),
        null
    );

    connection.initiateServerboundPlayConnection(host, port, handshake);

    // ✅ 使用反射设置 Minecraft.pendingConnection
    try {
        Field pendingConnectionField = Minecraft.class.getDeclaredField("pendingConnection");
        pendingConnectionField.setAccessible(true);
        pendingConnectionField.set(client, connection);
    } catch (Exception e) {
        System.err.println("[Reload Everything] 无法设置 pendingConnection: " + e.getMessage());
        e.printStackTrace();
    }
}
    // ==================== 软重启（重新加载资源） ====================
    public static void softReload() {
        if (clientReloadCallback != null) {
            clientReloadCallback.run();
        } else {
            System.out.println("[Reload Everything] 客户端回调未注册，无法执行软重启");
        }
    }

    // ==================== 真重启（进程级重启） ====================
    public static void restartGame() {
        if (isDevelopmentEnvironment()) {
            System.out.println("[Reload Everything] 开发环境不支持真重启");
            return;
        }

        String command = ReloadEverythingPreLaunch.getLaunchCommand();
        if (command == null || command.isEmpty()) {
            System.out.println("[Reload Everything] 未捕获到启动命令，执行软重启");
            softReload();
            return;
        }

        String[] parts = command.split(" ");
        if (parts.length == 0) {
            System.out.println("[Reload Everything] 启动命令为空，执行软重启");
            softReload();
            return;
        }

        String mainClass = parts[0];
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        boolean isModrinth = command.contains("com.modrinth.theseus.MinecraftLaunch");
        if (isModrinth) {
            System.out.println("[Reload Everything] 检测到 Modrinth 启动器，跳过启动器直接启动 KnotClient");
            mainClass = "net.fabricmc.loader.impl.launch.knot.KnotClient";
        }

        try {
            Class.forName(mainClass);
        } catch (ClassNotFoundException e) {
            System.out.println("[Reload Everything] 主类不存在: " + mainClass + "，尝试使用 KnotClient");
            mainClass = "net.fabricmc.loader.impl.launch.knot.KnotClient";
        }

        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(System.getProperty("java.home") + "/bin/java");

            Properties props = System.getProperties();
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                if (value != null && !key.startsWith("java.") && !key.startsWith("sun.") && !key.startsWith("jdk.")) {
                    if (key.contains("modrinth") || key.contains("theseus")) {
                        continue;
                    }
                    cmd.add("-D" + key + "=" + value);
                }
            }

            cmd.add("-cp");
            cmd.add(System.getProperty("java.class.path"));
            cmd.add(mainClass);
            cmd.addAll(Arrays.asList(args));

            System.out.println("[Reload Everything] 执行重启命令: " + String.join(" ", cmd));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.inheritIO();
            Process process = pb.start();
            Thread.sleep(500);
            if (process.isAlive() || process.exitValue() != 0) {
                System.out.println("[Reload Everything] 新进程已启动，退出当前游戏");
                System.exit(0);
            } else {
                System.out.println("[Reload Everything] 新进程启动失败，执行软重启");
                softReload();
            }
        } catch (Exception e) {
            System.err.println("[Reload Everything] 重启失败: " + e.getMessage());
            e.printStackTrace();
            softReload();
        }
    }
}