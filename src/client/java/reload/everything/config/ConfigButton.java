package reload.everything.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 各界面通用的 ⚙ 配置按钮。
 */
public final class ConfigButton {

    private ConfigButton() {
    }

    public static Button create(Screen parent, int x, int y, int size) {
        Button button = Button.builder(
                        Component.literal("⚙"),
                        b -> Minecraft.getInstance().gui.setScreen(ReloadEverythingConfigScreen.create(parent)))
                .bounds(x, y, size, size)
                .build();
        button.setTooltip(Tooltip.create(Component.translatable("tooltip.reload-everything.config")));
        return button;
    }
}
