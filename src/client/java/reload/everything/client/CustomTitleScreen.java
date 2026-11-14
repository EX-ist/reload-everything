package reload.everything.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import reload.everything.config.ReloadEverythingConfig;
import reload.everything.core.RestartManager;

public class CustomTitleScreen extends TitleScreen {

    public CustomTitleScreen(boolean fading) {
        super(fading);
    }

    @Override
    protected void init() {
        super.init();

        // ==================== 主菜单重启按钮 + 配置按钮 ====================
        ReloadEverythingConfig config = ReloadEverythingConfig.get();
        boolean softReload = config.restartMode == ReloadEverythingConfig.RestartMode.SOFT_RELOAD;

        int buttonSize = 20;
        int gap = 4;
        int y = this.height / 4 + 144;
        int exitX = this.width / 2 + 2;
        int exitWidth = 98;
        int x = exitX + exitWidth + gap;

        if (config.showTitleScreenButton) {
            Button restartButton = Button.builder(
                    Component.literal("⟳"),
                    button -> RestartConfirmScreen.confirmOrRun(
                            this,
                            softReload ? RestartManager::softReload : RestartManager::restartGame,
                            softReload
                                    ? "screen.reload-everything.confirm.soft_reload"
                                    : "screen.reload-everything.confirm.restart"))
                    .bounds(x, y, buttonSize, buttonSize)
                    .build();
            restartButton.setTooltip(Tooltip.create(Component.translatable(
                    softReload ? "tooltip.reload-everything.soft_reload" : "tooltip.reload-everything.restart_game")));
            this.addRenderableWidget(restartButton);
        }
    }
}