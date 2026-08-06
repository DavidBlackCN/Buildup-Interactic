package interactic;

import interactic.util.InteracticNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ItemFilterScreen extends AbstractContainerScreen<ItemFilterScreenHandler> {

    private static final Identifier TEXTURE = InteracticInit.id("textures/gui/item_filter.png");

    public boolean blockMode = true;

    private Button blockButton = null;
    private Button allowButton = null;

    public ItemFilterScreen(ItemFilterScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 178);
        this.inventoryLabelY = 69420;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.addRenderableWidget(this.blockButton = Button.builder(Component.translatable("screen.interactic.item_filter.block"), button -> sendModeRequest(true)).bounds(this.leftPos + 43, this.topPos + 78, 60, 12).build());
        this.addRenderableWidget(this.allowButton = Button.builder(Component.translatable("screen.interactic.item_filter.allow"), button -> sendModeRequest(false)).bounds(this.leftPos + 108, this.topPos + 78, 60, 12).build());
    }

    private static void sendModeRequest(boolean mode) {
        InteracticNetworking.sendFilterMode(mode);
    }

    @SuppressWarnings({"ConstantConditions"})
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.blockButton.active = !this.blockMode;
        this.allowButton.active = this.blockMode;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);
        context.text(this.minecraft.font, Component.translatable("screen.interactic.item_filter.mode"), 8, 80, 0xFF404040, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if (!this.blockMode) {
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 7, this.topPos + 19, 0, 178, 162, 54, 256, 256);
        }
    }
}
