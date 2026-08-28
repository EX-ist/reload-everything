package reload.everything.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import reload.everything.core.RestartManager;

public class CustomPauseScreen extends PauseScreen {

    public CustomPauseScreen(boolean showPauseMenu) {
        super(showPauseMenu);
    }

    @Override
protected void init() {
    super.init();

    Minecraft client = Minecraft.getInstance();
    boolean isSingleplayer = client.isLocalServer();
    boolean isMultiplayer = client.getCurrentServer() != null;

    // 原版“选项”按钮的位置：(width/2 - 100, height/4 - 16 + 96)
    int optionsX = this.width / 2 - 100;
    int optionsY = this.height / 4 - 16 + 96;

    int buttonSize = 20;
    int gap = 4;
    int currentX = optionsX - gap;

    // ==================== 重载存档按钮（单机模式下显示） ====================
    if (isSingleplayer) {
        Button reloadWorldButton = Button.builder(
                Component.literal("⟳"),
                button -> {
                    client.gui.setScreen(new RestartConfirmScreen(this, RestartManager::reloadWorld));
                })
                .bounds(currentX - buttonSize, optionsY, buttonSize, buttonSize)
                .build();
        reloadWorldButton.setTooltip(Tooltip.create(Component.literal("重载当前存档")));
        this.addRenderableWidget(reloadWorldButton);
        currentX = currentX - buttonSize - gap;
    }

    // ==================== 重连服务器按钮（多人模式下显示） ====================
    if (isMultiplayer) {
        Button reconnectButton = Button.builder(
                Component.literal("↻"),
                button -> {
                    client.gui.setScreen(new RestartConfirmScreen(this, RestartManager::reconnectServer));
                })
                .bounds(currentX - buttonSize, optionsY, buttonSize, buttonSize)
                .build();
        reconnectButton.setTooltip(Tooltip.create(Component.literal("重新连接服务器")));
        this.addRenderableWidget(reconnectButton);
    }
}
}