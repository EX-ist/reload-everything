package reload.everything;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.util.ArrayList;
import java.util.List;

public class ReloadEverythingPreLaunch implements PreLaunchEntrypoint {
    private static String fullCommand = "";
    private static String mainClass = "";
    private static String[] allArgs = new String[0];

    @Override
    public void onPreLaunch() {
        fullCommand = System.getProperty("sun.java.command", "");
        System.out.println("[Reload Everything] sun.java.command: " + fullCommand);

        if (!fullCommand.isEmpty()) {
            String[] parsed = parseCommand(fullCommand);
            if (parsed.length > 0) {
                mainClass = parsed[0];
                allArgs = new String[parsed.length - 1];
                System.arraycopy(parsed, 1, allArgs, 0, parsed.length - 1);
            }
            System.out.println("[Reload Everything] 解析后参数数量: " + allArgs.length);
        }
    }

    /**
     * 智能解析命令行：合并带空格的路径参数
     */
    private static String[] parseCommand(String command) {
        String[] tokens = command.split(" ");
        List<String> result = new ArrayList<>();
        int i = 0;

        while (i < tokens.length) {
            String token = tokens[i];
            if (isPathParam(token)) {
                // 路径参数，合并后续部分（直到遇到下一个以 -- 开头的参数）
                result.add(token);
                i++;
                StringBuilder path = new StringBuilder();
                while (i < tokens.length && !tokens[i].startsWith("--")) {
                    if (path.length() > 0) {
                        path.append(" ");
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

    /**
     * 判断是否为路径参数（其值可能包含空格）
     */
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