package reload.everything.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import reload.everything.core.RestartManager;

public class CustomTitleScreen extends TitleScreen {

    public CustomTitleScreen(boolean fading) {
        super(fading);
    }

    @Override
    protected void init() {
        super.init();

        // 三个小图标（friends、language、accessibility）的 y 坐标
        // 在原版 TitleScreen.init() 中，它们位于最后一组按钮的 y 位置
        // 具体值：this.height / 4 + 48 + 72 = this.height / 4 + 120
        int iconY = this.height / 4 + 120;

        int buttonSize = 20;
        int gap = 4;
        int numberOfOriginalIcons = 3;
        int totalWidth = numberOfOriginalIcons * buttonSize + (numberOfOriginalIcons - 1) * gap;
        int startX = this.width / 2 - totalWidth / 2;
        // 重启按钮是第四个
        int restartX = startX + numberOfOriginalIcons * (buttonSize + gap);

        Button restartButton = Button.builder(
                Component.literal("⟳"),
                button -> {
                    Minecraft.getInstance().gui.setScreen(new RestartConfirmScreen(this));
                })
                .bounds(restartX, iconY, buttonSize, buttonSize)
                .build();
        this.addRenderableWidget(restartButton);
    }
}