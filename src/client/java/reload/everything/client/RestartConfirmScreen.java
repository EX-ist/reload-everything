package reload.everything.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import reload.everything.core.RestartManager;

public class RestartConfirmScreen extends Screen {
    private final Screen parent;

    public RestartConfirmScreen(Screen parent) {
        super(Component.translatable("screen.reload-everything.confirm"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        Button confirmButton = Button.builder(
                Component.literal("确认重启"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.gui.setScreen(null);
                    }
                    RestartManager.restartGame();
                })
                .bounds(this.width / 2 - 105, this.height / 2 + 30, 100, 20)
                .build();
        this.addRenderableWidget(confirmButton);

        Button cancelButton = Button.builder(
                Component.literal("取消"),
                button -> this.onClose())
                .bounds(this.width / 2 + 5, this.height / 2 + 30, 100, 20)
                .build();
        this.addRenderableWidget(cancelButton);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        Component title = Component.literal("确定要重启游戏吗？");
        int titleWidth = this.font.width(title);
        graphics.text(this.font, title, this.width / 2 - titleWidth / 2, this.height / 2 - 50, 0xFFFFFF, true);

        Component hint = Component.literal("游戏将关闭并重新启动");
        int hintWidth = this.font.width(hint);
        graphics.text(this.font, hint, this.width / 2 - hintWidth / 2, this.height / 2 - 25, 0xAAAAAA, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}