package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.Helpers;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0, shift = At.Shift.AFTER))
    private void renderItemTooltip(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (!InteracticInit.getConfig().renderItemTooltips()) return;

        final var client = Minecraft.getInstance();
        final var camera = client.getCameraEntity();
        if (camera == null) return;

        final var item = Helpers.raycastItem(camera, 5);

        if (item == null) return;
        var text = item.getItem().getHoverName();
        context.drawString(client.font, text, context.guiWidth() / 2 - client.font.width(text) / 2, context.guiHeight() / 2 + 15, 0xFFFFFF, true);
    }

}
