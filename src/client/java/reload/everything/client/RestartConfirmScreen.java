package reload.everything.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RestartConfirmScreen extends Screen {

    private final Screen parent;
    private final Runnable onConfirm;
    private final String messageKey;
    private boolean confirmed;

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
                    // 防连点：动作可能触发重启/重载，重复执行会导致二次保存或二次拉起
                    if (this.confirmed) {
                        return;
                    }
                    this.confirmed = true;
                    button.active = false;
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
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        String text = Component.translatable(this.messageKey).getString();
        int x = this.width / 2 - this.font.width(text) / 2;
        graphics.text(this.font, text, x, this.height / 2 - 50, 0xFFFFFFFF, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
