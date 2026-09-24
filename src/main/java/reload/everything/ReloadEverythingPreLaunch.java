package reload.everything;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class ReloadEverythingPreLaunch implements PreLaunchEntrypoint {
    private static String fullCommand = "";
    private static String mainClass = "";
    private static String[] allArgs = new String[0];

    @Override
    public void onPreLaunch() {
        fullCommand = System.getProperty("sun.java.command", "");
        System.out.println("[Reload Everything] sun.java.command: " + fullCommand);

        // 解析命令：主类 + 参数列表
        if (!fullCommand.isEmpty()) {
            String[] parts = fullCommand.split(" ");
            if (parts.length > 0) {
                mainClass = parts[0];
                if (parts.length > 1) {
                    allArgs = new String[parts.length - 1];
                    System.arraycopy(parts, 1, allArgs, 0, parts.length - 1);
                }
                System.out.println("[Reload Everything] 主类: " + mainClass);
                System.out.println("[Reload Everything] 参数数量: " + allArgs.length);
            }
        }
    }

    public static String getFullCommand() {
        return fullCommand;
    }

    public static String getMainClass() {
        return mainClass;
    }

    public static String[] getAllArgs() {
        return allArgs;
    }
}