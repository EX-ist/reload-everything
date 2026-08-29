package reload.everything.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class RestartConfirmScreen extends Screen {
    private final Screen parent;
    private final Runnable onConfirm;
    private final String messageKey;

    public RestartConfirmScreen(Screen parent, Runnable onConfirm, String messageKey) {
        super(Component.translatable("screen.reload-everything.confirm.title"));
        this.parent = parent;
        this.onConfirm = onConfirm;
        this.messageKey = messageKey;
    }

    @Override
    protected void init() {
        super.init();

        Button confirmButton = Button.builder(
                Component.translatable("screen.reload-everything.confirm.button.confirm"),
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
                Component.translatable("screen.reload-everything.confirm.button.cancel"),
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
        // 先调用 super 渲染背景和按钮
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        // 获取确认消息的文本
        Component message = Component.translatable(this.messageKey);
        String text = message.getString();
        int textWidth = this.font.width(text);
        int x = this.width / 2 - textWidth / 2;
        int y = this.height / 2 - 50;

        // 绘制文字（直接使用 String）
        graphics.text(this.font, text, x, y, 0xFFFFFFFF, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}