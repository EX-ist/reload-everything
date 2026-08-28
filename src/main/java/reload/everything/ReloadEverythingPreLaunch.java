package reload.everything;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class ReloadEverythingPreLaunch implements PreLaunchEntrypoint {
    private static String fullCommand = "";

    @Override
    public void onPreLaunch() {
        // 保存完整的启动命令
        fullCommand = System.getProperty("sun.java.command", "");
        System.out.println("[Reload Everything] 捕获启动命令: " + fullCommand);
    }

    public static String getLaunchCommand() {
        return fullCommand;
    }
}