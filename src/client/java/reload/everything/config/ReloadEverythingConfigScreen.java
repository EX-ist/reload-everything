package reload.everything.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * 基于 Cloth Config API 的配置界面。
 */
public final class ReloadEverythingConfigScreen {

    private ReloadEverythingConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ReloadEverythingConfig config = ReloadEverythingConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("reload-everything.config.title"))
                .setDoesConfirmSave(false)
                .setSavingRunnable(config::save);
        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory behavior = builder.getOrCreateCategory(Component.translatable("reload-everything.config.category.behavior"));
        behavior.addEntry(entries.startBooleanToggle(
                        Component.translatable("reload-everything.config.confirm_before_action"), config.confirmBeforeAction)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("reload-everything.config.confirm_before_action.tooltip"))
                .setSaveConsumer(value -> config.confirmBeforeAction = value)
                .build());
        behavior.addEntry(entries.startEnumSelector(
                        Component.translatable("reload-everything.config.restart_mode"),
                        ReloadEverythingConfig.RestartMode.class,
                        config.restartMode)
                .setDefaultValue(ReloadEverythingConfig.RestartMode.PROCESS_RESTART)
                .setEnumNameProvider(mode -> Component.translatable(
                        "reload-everything.config.restart_mode."
                                + ((ReloadEverythingConfig.RestartMode) mode).name().toLowerCase(Locale.ROOT)))
                .setTooltip(Component.translatable("reload-everything.config.restart_mode.tooltip"))
                .setSaveConsumer(value -> config.restartMode = value)
                .build());

        ConfigCategory buttons = builder.getOrCreateCategory(Component.translatable("reload-everything.config.category.buttons"));
        buttons.addEntry(entries.startBooleanToggle(
                        Component.translatable("reload-everything.config.show_title_screen_button"), config.showTitleScreenButton)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("reload-everything.config.show_title_screen_button.tooltip"))
                .setSaveConsumer(value -> config.showTitleScreenButton = value)
                .build());
        buttons.addEntry(entries.startBooleanToggle(
                        Component.translatable("reload-everything.config.show_pause_screen_button"), config.showPauseScreenButton)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("reload-everything.config.show_pause_screen_button.tooltip"))
                .setSaveConsumer(value -> config.showPauseScreenButton = value)
                .build());
        buttons.addEntry(entries.startBooleanToggle(
                        Component.translatable("reload-everything.config.show_resource_pack_button"), config.showResourcePackButton)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("reload-everything.config.show_resource_pack_button.tooltip"))
                .setSaveConsumer(value -> config.showResourcePackButton = value)
                .build());

        return builder.build();
    }
}
