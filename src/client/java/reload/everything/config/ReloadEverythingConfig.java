package reload.everything.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import reload.everything.ReloadEverything;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 模组配置，持久化到 config/reload-everything.json。
 */
public final class ReloadEverythingConfig {

    public enum RestartMode {
        /** 结束当前进程并重新启动一个 JVM。 */
        PROCESS_RESTART,
        /** 不退出进程，直接回到标题界面重新加载资源与数据。 */
        SOFT_RELOAD
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ReloadEverythingConfig instance;

    public boolean confirmBeforeAction = true;
    public boolean showTitleScreenButton = true;
    public boolean showPauseScreenButton = true;
    public boolean showResourcePackButton = true;
    public RestartMode restartMode = RestartMode.PROCESS_RESTART;

    public static ReloadEverythingConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("reload-everything.json");
    }

    private static ReloadEverythingConfig load() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) {
            return new ReloadEverythingConfig();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            ReloadEverythingConfig config = GSON.fromJson(reader, ReloadEverythingConfig.class);
            if (config == null) {
                return new ReloadEverythingConfig();
            }
            if (config.restartMode == null) {
                config.restartMode = RestartMode.PROCESS_RESTART;
            }
            return config;
        } catch (IOException | JsonSyntaxException e) {
            ReloadEverything.warn("Failed to read config file {}, using defaults", path);
            return new ReloadEverythingConfig();
        }
    }

    public void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            ReloadEverything.error("Failed to save config file " + path, e);
        }
    }
}
