package reload.everything.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import reload.everything.core.RestartManager;

public class ReloadEverythingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[Reload Everything] 客户端已初始化");

        RestartManager.setClientReloadCallback(() -> {
            Minecraft.getInstance().reloadResourcePacks();
            System.out.println("[Reload Everything] 资源已重新加载");
        });

        // ==================== 主菜单替换 ====================
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen && !(screen instanceof CustomTitleScreen)) {
                client.gui.setScreen(new CustomTitleScreen(true));
            }
        });

        // ==================== 暂停菜单替换 ====================
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen && !(screen instanceof CustomPauseScreen)) {
                client.gui.setScreen(new CustomPauseScreen(true));
            }
        });
    }
}