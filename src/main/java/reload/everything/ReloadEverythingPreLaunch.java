package reload.everything;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.util.ArrayList;
import java.util.List;

/**
 * 在游戏启动早期记录本次进程的启动命令，供 {@link reload.everything.core.RestartManager} 复现重启。
 * sun.java.command 拿不到 -Xmx 之类的 JVM 参数，所以重启时会优先使用 ProcessHandle 提供的真实 argv。
 */
public class ReloadEverythingPreLaunch implements PreLaunchEntrypoint {

    private static String fullCommand = "";
    private static String mainClass = "";
    private static String[] allArgs = new String[0];

    @Override
    public void onPreLaunch() {
        fullCommand = System.getProperty("sun.java.command", "");
        ReloadEverything.info("Launch command: {}", fullCommand);
        if (fullCommand.isEmpty()) {
            return;
        }

        String[] parsed = parseCommand(fullCommand);
        if (parsed.length > 0) {
            mainClass = parsed[0];
            allArgs = new String[parsed.length - 1];
            System.arraycopy(parsed, 1, allArgs, 0, parsed.length - 1);
        }
        ReloadEverything.info("Parsed main class: {}, argument count: {}", mainClass, allArgs.length);
    }

    /** 合并带空格的路径类参数（--gameDir 的值里可能有空格）。 */
    private static String[] parseCommand(String command) {
        String[] tokens = command.split(" ");
        List<String> result = new ArrayList<>();
        int i = 0;
        while (i < tokens.length) {
            String token = tokens[i];
            if (isPathParam(token)) {
                result.add(token);
                i++;
                StringBuilder path = new StringBuilder();
                while (i < tokens.length && !tokens[i].startsWith("--")) {
                    if (path.length() > 0) {
                        path.append(' ');
                    }
                    path.append(tokens[i]);
                    i++;
                }
                result.add(path.toString());
            } else {
                result.add(token);
                i++;
            }
        }
        return result.toArray(new String[0]);
    }

    private static boolean isPathParam(String token) {
        return token.equals("--gameDir")
                || token.equals("--assetsDir")
                || token.equals("--resourcePackDir")
                || token.equals("--dataPackDir")
                || token.equals("--libDir");
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
