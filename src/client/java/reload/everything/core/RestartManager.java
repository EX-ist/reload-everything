package reload.everything.core;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import reload.everything.ReloadEverythingPreLaunch;

import java.io.File;
import java.io.IOException;
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

    // ✅ 直接从 sun.java.command 获取完整命令
    String fullCommand = ReloadEverythingPreLaunch.getFullCommand();
    String mainClass = ReloadEverythingPreLaunch.getMainClass();
    String[] args = ReloadEverythingPreLaunch.getAllArgs();

    if (fullCommand.isEmpty() || mainClass.isEmpty()) {
        System.out.println("[Reload Everything] 无法获取启动命令，执行软重启");
        softReload();
        return;
    }

    System.out.println("[Reload Everything] 完整命令: " + fullCommand);

    // 检测到 Modrinth 启动器，直接使用 KnotClient
    if (mainClass.contains("com.modrinth.theseus.MinecraftLaunch")) {
        System.out.println("[Reload Everything] 检测到 Modrinth 启动器，跳过启动器直接启动 KnotClient");
        mainClass = "net.fabricmc.loader.impl.launch.knot.KnotClient";
        // 注意：args 中可能还包含启动器自身的参数，需要过滤？
        // 在 Modrinth 中，args 可能包含 "--username EX_es" 等，但前面可能有启动器参数
        // 目前保持原样，因为 args 是从 sun.java.command 中解析的，包含所有参数
    }

    try {
        List<String> cmd = new ArrayList<>();
        cmd.add(System.getProperty("java.home") + "/bin/java");

        // 传递系统属性（JVM 参数）
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

        // 传递 classpath
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));

        // 主类
        cmd.add(mainClass);

        // ✅ 直接传递所有参数（不修改、不重新排列）
        // 这样 --username EX_es 等键值对保持完整
        cmd.addAll(Arrays.asList(args));

        System.out.println("[Reload Everything] 执行重启命令:");
        System.out.println(String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(System.getProperty("user.dir")));
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