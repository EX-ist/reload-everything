package reload.everything.core;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import reload.everything.ReloadEverything;
import reload.everything.ReloadEverythingPreLaunch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public final class RestartManager {

    private static final String RESTARTED_FLAG = "reload.everything.restarted";
    private static final String FASTQUIT_CLASS = "me.contaria.fastquit.FastQuit";
    private static final String KNOT_CLIENT = "net.fabricmc.loader.impl.launch.knot.KnotClient";
    private static final String THESEUS_LAUNCH = "com.modrinth.theseus.MinecraftLaunch";
    private static final long SERVER_STOP_TIMEOUT_MS = 15_000L;
    private static final long RELAUNCH_DELAY_MS = 2_000L;

    /** KnotClient 需要的原版客户端参数，替换启动器主类时只有这些会被保留。 */
    private static final Set<String> MINECRAFT_CLIENT_ARGUMENTS = new LinkedHashSet<>(List.of(
            "--username", "--uuid", "--accessToken", "--clientId", "--xuid", "--userProperties",
            "--version", "--versionType", "--userType", "--gameDir", "--assetsDir", "--assetIndex",
            "--width", "--height", "--fullscreen", "--demo", "--lang", "--renderDistance",
            "--server", "--port", "--quickPlayPath", "--quickPlaySingleplayer",
            "--quickPlayMultiplayer", "--quickPlayRealms"));

    private static Runnable clientReloadCallback;

    private RestartManager() {
    }

    public static void setClientReloadCallback(Runnable callback) {
        clientReloadCallback = callback;
    }

    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    // ==================== 软重载（重新加载资源） ====================

    public static void softReload() {
        if (clientReloadCallback == null) {
            ReloadEverything.warn("Client reload callback not registered, cannot soft reload");
            return;
        }
        clientReloadCallback.run();
        ReloadEverything.info("Resources reloaded");
    }

    // ==================== 单人存档重载 ====================

    public static void reloadWorld() {
        Minecraft client = Minecraft.getInstance();
        if (!client.isLocalServer()) {
            ReloadEverything.warn("Not in a singleplayer world, cannot reload the world");
            return;
        }
        IntegratedServer server = client.getSingleplayerServer();
        if (server == null) {
            ReloadEverything.warn("Failed to get the integrated server instance");
            return;
        }

        String worldName = server.getWorldData().getLevelName();
        ReloadEverything.info("Reloading world: {}", worldName);
        client.disconnectFromWorld(Component.translatable("reload-everything.status.reloading_world"));

        if (!awaitServerStop(client, server)) {
            ReloadEverything.error("Timed out waiting for the world to save, reload cancelled to avoid corrupting the save");
            return;
        }

        clearFastQuitSavingWorlds();
        ReloadEverything.info("Loading world: {}", worldName);
        client.createWorldOpenFlows().openWorld(worldName,
                () -> ReloadEverything.info("World reloaded: {}", worldName));
    }

    /**
     * 等待单人服务器真正停止（存档落盘完成）。
     * 装了 FastQuit 时走它的阻塞式 wait()，否则轮询服务器实例，超时即视为失败。
     */
    private static boolean awaitServerStop(Minecraft client, IntegratedServer server) {
        if (waitWithFastQuit(server)) {
            return true;
        }
        long deadline = System.currentTimeMillis() + SERVER_STOP_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (client.getSingleplayerServer() == null || !server.isRunning()) {
                return true;
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return client.getSingleplayerServer() == null;
    }

    private static boolean waitWithFastQuit(IntegratedServer server) {
        try {
            Class<?> fastQuit = Class.forName(FASTQUIT_CLASS);
            fastQuit.getMethod("wait", IntegratedServer.class).invoke(null, server);
            ReloadEverything.info("FastQuit finished saving the world");
            return true;
        } catch (ClassNotFoundException e) {
            ReloadEverything.info("FastQuit not installed, polling until the world is saved");
            return false;
        } catch (Throwable t) {
            ReloadEverything.warn("FastQuit.wait failed: {}", String.valueOf(t));
            return false;
        }
    }

    /**
     * 清空 FastQuit 的 savingWorlds 记账。
     * FastQuit.wait() 返回后可能残留一条永远清不掉的条目，会让 openWorld 误报"另一个世界正在保存"。
     * 此时服务器已 shutdown、存档已落盘，清掉残留只是修账，不会丢数据。
     */
    private static void clearFastQuitSavingWorlds() {
        try {
            Class<?> fastQuit = Class.forName(FASTQUIT_CLASS);
            var field = fastQuit.getDeclaredField("savingWorlds");
            field.setAccessible(true);
            if (field.get(null) instanceof java.util.Map<?, ?> map && !map.isEmpty()) {
                int size = map.size();
                map.clear();
                ReloadEverything.info("Cleared FastQuit savingWorlds ({} leftover entries)", size);
            }
        } catch (ClassNotFoundException e) {
            // FastQuit 未安装，无需处理
        } catch (Throwable t) {
            ReloadEverything.warn("Failed to clear FastQuit savingWorlds: {}", String.valueOf(t));
        }
    }

    // ==================== 进程级重启 ====================

    public static void restartGame() {
        if (isDevelopmentEnvironment()) {
            ReloadEverything.warn("Process restart is not supported in a development environment, use soft reload instead");
            return;
        }
        if (Boolean.getBoolean(RESTARTED_FLAG)) {
            ReloadEverything.warn("Restart loop detected (marker file present), falling back to soft reload");
            softReload();
            return;
        }

        List<String> command = buildRestartCommand();
        if (command == null) {
            ReloadEverything.warn("Failed to restore the launch command, falling back to soft reload");
            softReload();
            return;
        }

        ReloadEverything.info("Restarting, new process command: {}", String.join(" ", command));
        // 先退出再拉起：避免新旧两个实例同时写 options.txt / logs / saves
        Runtime.getRuntime().addShutdownHook(new Thread(() -> relaunch(command), "reload-everything-relaunch"));
        System.exit(0);
    }

    private static void relaunch(List<String> command) {
        try {
            Thread.sleep(RELAUNCH_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(new File(System.getProperty("user.dir", ".")));
            builder.inheritIO();
            Process process = builder.start();
            ReloadEverything.info("New process started, pid={}", process.pid());
        } catch (Exception e) {
            ReloadEverything.error("Failed to start the new process, the game will exit without restarting", e);
        }
    }

    private static List<String> buildRestartCommand() {
        String classpath = System.getProperty("java.class.path");
        if (classpath == null || classpath.isBlank()) {
            return null;
        }

        String mainClass = ReloadEverythingPreLaunch.getMainClass();
        String[] rawArguments = rawLaunchArguments();
        List<String> jvmArguments = new ArrayList<>();
        List<String> gameArguments = new ArrayList<>();

        if (rawArguments != null && !mainClass.isEmpty()) {
            List<String> raw = new ArrayList<>(Arrays.asList(rawArguments));
            int mainIndex = raw.indexOf(mainClass);
            if (mainIndex > 0) {
                jvmArguments.addAll(sanitizeJvmArguments(raw.subList(0, mainIndex)));
                gameArguments.addAll(raw.subList(mainIndex + 1, raw.size()));
            }
        }

        if (gameArguments.isEmpty()) {
            // 回退：从 sun.java.command 还原（拿不到 -Xmx 等 JVM 参数，用当前堆上限兜底）
            if (mainClass.isEmpty()) {
                return null;
            }
            jvmArguments.clear();
            jvmArguments.addAll(sanitizeJvmArguments(systemPropertyArguments()));
            gameArguments.addAll(Arrays.asList(ReloadEverythingPreLaunch.getAllArgs()));
        }

        String effectiveMainClass = mainClass;
        if (effectiveMainClass.contains(THESEUS_LAUNCH)) {
            ReloadEverything.info("Modrinth launcher detected, launching through KnotClient directly");
            effectiveMainClass = KNOT_CLIENT;
            gameArguments = filterMinecraftArguments(gameArguments);
        }

        ensureMaximumHeap(jvmArguments);

        List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        command.addAll(jvmArguments);
        command.add("-D" + RESTARTED_FLAG + "=true");
        command.add("-cp");
        command.add(classpath);
        command.add(effectiveMainClass);
        command.addAll(gameArguments);
        return command;
    }

    /** 当前进程的真实 argv（含 -Xmx 等 JVM 参数）；平台不支持时返回 null。 */
    private static String[] rawLaunchArguments() {
        try {
            Optional<String[]> arguments = ProcessHandle.current().info().arguments();
            if (arguments.isPresent() && arguments.get().length > 0) {
                return arguments.get();
            }
        } catch (Throwable t) {
            ReloadEverything.warn("Failed to read process arguments: {}", String.valueOf(t));
        }
        return null;
    }

    private static List<String> systemPropertyArguments() {
        List<String> result = new ArrayList<>();
        Properties properties = System.getProperties();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value == null || key.startsWith("java.") || key.startsWith("sun.") || key.startsWith("jdk.")) {
                continue;
            }
            if (key.contains("modrinth") || key.contains("theseus")) {
                continue;
            }
            result.add("-D" + key + "=" + value);
        }
        return result;
    }

    private static List<String> sanitizeJvmArguments(List<String> arguments) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            // 类路径由本方法统一补齐，避免重复
            if (argument.equals("-cp") || argument.equals("-classpath") || argument.equals("--class-path")) {
                i++;
                continue;
            }
            if (argument.startsWith("-Dtheseus") || argument.startsWith("-Dmodrinth")
                    || argument.startsWith("-D" + RESTARTED_FLAG)) {
                continue;
            }
            result.add(argument);
        }
        return result;
    }

    private static List<String> filterMinecraftArguments(List<String> arguments) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            if (!MINECRAFT_CLIENT_ARGUMENTS.contains(argument)) {
                ReloadEverything.info("Dropping launcher-specific argument: {}", argument);
                continue;
            }
            result.add(argument);
            if (i + 1 < arguments.size()) {
                result.add(arguments.get(++i));
            }
        }
        return result;
    }

    /** 保证新进程至少拥有与当前进程相同的堆上限，避免大整合包重启后 OOM。 */
    private static void ensureMaximumHeap(List<String> jvmArguments) {
        for (String argument : jvmArguments) {
            if (argument.startsWith("-Xmx")) {
                return;
            }
        }
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory > 0L) {
            jvmArguments.add("-Xmx" + (maxMemory / (1024L * 1024L)) + "M");
        }
    }

    private static String javaExecutable() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win") ? "java.exe" : "java";
        return new File(new File(System.getProperty("java.home"), "bin"), executable).getAbsolutePath();
    }
}
