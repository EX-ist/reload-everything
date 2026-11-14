package reload.everything.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import reload.everything.ReloadEverything;
import reload.everything.config.ConfigButton;
import reload.everything.config.ReloadEverythingConfig;

import java.nio.file.Path;
import java.util.function.Consumer;

public class CustomResourcePackScreen extends PackSelectionScreen {

    private final Screen returnScreen;

    public CustomResourcePackScreen(PackRepository repository, Consumer<PackRepository> output, Path packDir,
                                    Component title, Screen returnScreen) {
        super(repository, output, packDir, title);
        this.returnScreen = returnScreen;
    }

    @Override
    protected void init() {
        super.init();

        Minecraft client = Minecraft.getInstance();

        // 以原版"打开资源包文件夹"按钮为基准定位：紧贴它左侧、同一行、同一高度。
        Button folderButton = null;
        String folderLabel = Component.translatable("pack.openFolder").getString();
        for (var child : this.children()) {
            if (child instanceof Button candidate && candidate.getMessage().getString().equals(folderLabel)) {
                folderButton = candidate;
                break;
            }
        }

        int buttonSize = 20;
        int gap = 4;
        int x;
        int y;
        if (folderButton != null) {
            buttonSize = folderButton.getHeight();
            x = folderButton.getX() - gap - buttonSize;
            y = folderButton.getY();
        } else {
            ReloadEverything.warn("未找到打开资源包文件夹按钮，⟳ 按钮回退到默认位置");
            x = 4;
            y = this.height - 33;
        }
        x = Math.max(4, x);

        ReloadEverythingConfig config = ReloadEverythingConfig.get();

        if (config.showResourcePackButton) {
            Button reloadButton = Button.builder(
                    Component.literal("⟳"),
                    button -> RestartConfirmScreen.confirmOrRun(
                            this,
                            () -> {
                                button.active = false;
                                client.reloadResourcePacks().thenRun(() -> client.execute(() -> {
                                    ReloadEverything.info("资源包已重新加载");
                                    Screen target = this.returnScreen != null ? this.returnScreen : new CustomTitleScreen(true);
                                    client.gui.setScreen(target);
                                }));
                            },
                            "screen.reload-everything.confirm.reload_resource_packs"))
                    .bounds(x, y, buttonSize, buttonSize)
                    .build();
            reloadButton.setTooltip(Tooltip.create(Component.translatable("tooltip.reload-everything.reload_resource_packs")));
            this.addRenderableWidget(reloadButton);
            x -= buttonSize + gap;
        }

        this.addRenderableWidget(ConfigButton.create(this, Math.max(4, x), y, buttonSize));
    }
}
