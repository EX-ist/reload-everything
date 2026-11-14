package reload.everything.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import reload.everything.ReloadEverything;

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

        int buttonSize = 20;
        int gap = 4;
        int openFolderX = 8;
        int openFolderY = this.height - 33;
        int openFolderWidth = 98;

        Button reloadButton = Button.builder(
                Component.literal("⟳"),
                button -> {
                    button.active = false;
                    client.reloadResourcePacks().thenRun(() -> client.execute(() -> {
                        ReloadEverything.info("资源包已重新加载");
                        Screen target = this.returnScreen != null ? this.returnScreen : new CustomTitleScreen(true);
                        client.gui.setScreen(target);
                    }));
                })
                .bounds(openFolderX + openFolderWidth + gap, openFolderY, buttonSize, buttonSize)
                .build();
        reloadButton.setTooltip(Tooltip.create(Component.translatable("tooltip.reload-everything.reload_resource_packs")));
        this.addRenderableWidget(reloadButton);
    }
}
