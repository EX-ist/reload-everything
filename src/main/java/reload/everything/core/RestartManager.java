package reload.everything.core;

import net.fabricmc.loader.api.FabricLoader;
import reload.everything.ReloadEverythingPreLaunch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestartManager {

    // 客户端回调接口（由客户端模块实现）
    private static Runnable clientReloadCallback = null;

    public static void setClientReloadCallback(Runnable callback) {
        clientReloadCallback = callback;
    }

    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    /**
     * 真重启：关闭当前游戏，启动新进程
     */
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

    // 验证主类是否存在
    try {
        Class.forName(mainClass);
    } catch (ClassNotFoundException e) {
        System.out.println("[Reload Everything] 主类不存在: " + mainClass + "，尝试使用 KnotClient");
        mainClass = "net.fabricmc.loader.impl.launch.knot.KnotClient";
    }

    try {
        List<String> cmd = new ArrayList<>();
        cmd.add(System.getProperty("java.home") + "/bin/java");
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add(mainClass);
        cmd.addAll(Arrays.asList(args));

        System.out.println("[Reload Everything] 执行重启命令: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        Process process = pb.start();
        // 等待一小段时间确保新进程启动
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
        softReload(); // 失败时回退到软重启
    }
}

    /**
     * 软重启：重新加载资源（通过回调由客户端实现）
     */
    public static void softReload() {
        if (clientReloadCallback != null) {
            clientReloadCallback.run();
        } else {
            System.out.println("[Reload Everything] 客户端回调未注册，无法执行软重启");
        }
    }
}