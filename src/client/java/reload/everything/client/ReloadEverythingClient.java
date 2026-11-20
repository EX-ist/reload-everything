package reload.everything.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import reload.everything.ReloadEverything;
import reload.everything.core.RestartManager;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ReloadEverythingClient implements ClientModInitializer {

    /** 最近一个不是资源包界面的屏幕，作为资源包界面的返回目标。 */
    private static Screen lastNonPackScreen;

    @Override
    public void onInitializeClient() {
        RestartManager.setClientReloadCallback(() -> {
            Minecraft.getInstance().reloadResourcePacks();
            ReloadEverything.info("Resources reloaded");
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof PackSelectionScreen)) {
                lastNonPackScreen = screen;
            }

            if (screen instanceof TitleScreen && !(screen instanceof CustomTitleScreen)) {
                client.gui.setScreen(new CustomTitleScreen(true));
                return;
            }
            if (screen instanceof PauseScreen && !(screen instanceof CustomPauseScreen)) {
                client.gui.setScreen(new CustomPauseScreen(true));
                return;
            }
            if (screen instanceof PackSelectionScreen && !(screen instanceof CustomResourcePackScreen)) {
                replacePackSelectionScreen(client, (PackSelectionScreen) screen);
            }
        });
    }

    public static Screen getLastNonPackScreen() {
        return lastNonPackScreen;
    }

    /**
     * 用带 ⟳ 按钮的子类替换原版资源包界面。
     * 原版界面没有公开的构造入口参数，只能通过反射取出它已经持有的仓库/输出回调/目录/标题。
     */
    private static void replacePackSelectionScreen(Minecraft client, PackSelectionScreen screen) {
        try {
            Object model = readField(PackSelectionScreen.class, screen, "model");
            if (model == null) {
                throw new IllegalStateException("PackSelectionScreen.model is null");
            }
            PackRepository repository = (PackRepository) readField(model.getClass(), model, "repository");
            Consumer<PackRepository> output = (Consumer<PackRepository>) readField(model.getClass(), model, "output");
            Path packDir = (Path) readField(PackSelectionScreen.class, screen, "packDir");
            Component title = (Component) readField(Screen.class, screen, "title");

            client.gui.setScreen(new CustomResourcePackScreen(repository, output, packDir, title, lastNonPackScreen));
        } catch (Exception e) {
            ReloadEverything.error("Failed to replace the resource pack screen, keeping the vanilla one", e);
        }
    }

    private static Object readField(Class<?> owner, Object instance, String name) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(instance);
    }
}
