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

        int optionsX = this.width / 2 - 100;
        int optionsY = this.height / 4 - 16 + 96;

        int buttonSize = 20;
        int gap = 4;
        int currentX = optionsX - gap;

        // 只在单机模式下显示重载按钮
        if (isSingleplayer) {
            Button reloadWorldButton = Button.builder(
                    Component.literal("⟳"),
                    button -> {
                        client.gui.setScreen(new RestartConfirmScreen(
                                this,
                                RestartManager::reloadWorld,
                                "screen.reload-everything.confirm.reload_world"
                        ));
                    })
                    .bounds(currentX - buttonSize, optionsY, buttonSize, buttonSize)
                    .build();
            reloadWorldButton.setTooltip(Tooltip.create(Component.translatable("tooltip.reload-everything.reload_world")));
            this.addRenderableWidget(reloadWorldButton);
        }

        // 多人重连按钮已移除
    }
}