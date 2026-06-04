package interactic;

import interactic.util.InteracticNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ItemFilterScreen extends AbstractContainerScreen<ItemFilterScreenHandler> {

    private static final Identifier TEXTURE = InteracticInit.id("textures/gui/item_filter.png");

    public boolean blockMode = true;

    private Button blockButton = null;
    private Button allowButton = null;

    public ItemFilterScreen(ItemFilterScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageHeight = 178;
        this.inventoryLabelY = 69420;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.addRenderableWidget(this.blockButton = Button.builder(Component.literal("Block"), button -> sendModeRequest(true)).bounds(this.leftPos + 43, this.topPos + 78, 60, 12).build());
        this.addRenderableWidget(this.allowButton = Button.builder(Component.literal("Allow"), button -> sendModeRequest(false)).bounds(this.leftPos + 108, this.topPos + 78, 60, 12).build());
    }

    private static void sendModeRequest(boolean mode) {
        InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.FilterModeRequest(mode));
    }

    @SuppressWarnings({"ConstantConditions"})
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawString(this.minecraft.font, "Mode", this.leftPos + 8, this.topPos + 80, 0x404040, false);

        this.renderTooltip(context, mouseX, mouseY);

        this.blockButton.active = !this.blockMode;
        this.allowButton.active = this.blockMode;
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        context.blit(TEXTURE, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0, 0, this.imageWidth / 256f, this.imageHeight / 256f);

        if (!this.blockMode) {
            context.blit(TEXTURE, this.leftPos + 7, this.topPos + 19, 162, 54, 0, 178 / 256f, 162 / 256f, (178 + 54) / 256f);
        }
    }
}
