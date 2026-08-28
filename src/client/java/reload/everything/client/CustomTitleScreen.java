package reload.everything.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import reload.everything.core.RestartManager;
import net.minecraft.client.gui.components.Tooltip;

public class CustomTitleScreen extends TitleScreen {

    public CustomTitleScreen(boolean fading) {
        super(fading);
    }

    @Override
    protected void init() {
        super.init();

        // ==================== 主菜单重启按钮 ====================
        int buttonSize = 20;
        int y = this.height / 4 + 144;
        int exitX = this.width / 2 + 2;
        int exitWidth = 98;
        int restartX = exitX + exitWidth + 4;

        Button restartButton = Button.builder(
                Component.literal("⟳"),
                button -> {
                    Minecraft.getInstance().gui.setScreen(new RestartConfirmScreen(this, RestartManager::restartGame));
                })
                .bounds(restartX, y, buttonSize, buttonSize)
                .build();

        restartButton.setTooltip(Tooltip.create(Component.literal("重启游戏")));

        this.addRenderableWidget(restartButton);
    }
}