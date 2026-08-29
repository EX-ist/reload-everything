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
import reload.everything.core.RestartManager;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ReloadEverythingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[Reload Everything] 客户端已初始化");

        RestartManager.setClientReloadCallback(() -> {
            Minecraft.getInstance().reloadResourcePacks();
            System.out.println("[Reload Everything] 资源已重新加载");
        });

        // 主菜单替换
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen && !(screen instanceof CustomTitleScreen)) {
                client.gui.setScreen(new CustomTitleScreen(true));
            }
        });

        // 暂停菜单替换
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen && !(screen instanceof CustomPauseScreen)) {
                client.gui.setScreen(new CustomPauseScreen(true));
            }
        });

        // 资源包界面替换
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            System.out.println("[Reload Everything] AFTER_INIT 触发，屏幕类: " + screen.getClass().getName());
            if (screen instanceof PackSelectionScreen && !(screen instanceof CustomResourcePackScreen)) {
                PackSelectionScreen packScreen = (PackSelectionScreen) screen;
                try {
                    Field modelField = PackSelectionScreen.class.getDeclaredField("model");
                    modelField.setAccessible(true);
                    Object model = modelField.get(packScreen);

                    Field repositoryField = model.getClass().getDeclaredField("repository");
                    repositoryField.setAccessible(true);
                    PackRepository repository = (PackRepository) repositoryField.get(model);

                    Field packDirField = PackSelectionScreen.class.getDeclaredField("packDir");
                    packDirField.setAccessible(true);
                    Path packDir = (Path) packDirField.get(packScreen);

                    Field titleField = Screen.class.getDeclaredField("title");
                    titleField.setAccessible(true);
                    Component title = (Component) titleField.get(packScreen);

                    Field outputField = model.getClass().getDeclaredField("output");
                    outputField.setAccessible(true);
                    Consumer<PackRepository> output = (Consumer<PackRepository>) outputField.get(model);
                    Screen returnScreen = client.gui.screen();
                    client.gui.setScreen(new CustomResourcePackScreen(repository, output, packDir, title, returnScreen));
                } catch (Exception e) {
                    System.err.println("[Reload Everything] 无法替换资源包界面: " + e.getMessage());
                }
            }
        });
    }
}