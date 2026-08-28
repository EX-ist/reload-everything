package reload.everything.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RestartConfirmScreen extends Screen {
    private final Screen parent;
    private final Runnable onConfirm;

    public RestartConfirmScreen(Screen parent, Runnable onConfirm) {
        super(Component.translatable("screen.reload-everything.confirm"));
        this.parent = parent;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        super.init();

        Button confirmButton = Button.builder(
                Component.literal("确认"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.gui.setScreen(null);
                    }
                    if (this.onConfirm != null) {
                        this.onConfirm.run();
                    }
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

        Component title = Component.literal("确定要执行此操作吗？");
        int titleWidth = this.font.width(title);
        graphics.text(this.font, title, this.width / 2 - titleWidth / 2, this.height / 2 - 50, 0xFFFFFF, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}