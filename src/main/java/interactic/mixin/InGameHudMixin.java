package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.Helpers;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"))
    private void renderItemTooltip(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (!InteracticInit.getConfig().renderItemTooltips()) return;

        final var client = Minecraft.getInstance();
        final var item = Helpers.raycastItem(client.getCameraEntity(), 5);

        if (item == null) return;
        var tooltip = InteracticInit.getConfig().renderFullTooltip()
                ? item.getItem().getTooltipLines(Item.TooltipContext.EMPTY, client.player, TooltipFlag.NORMAL)
                : List.of(item.getItem().getHoverName());

        for (int i = 0, tooltipSize = tooltip.size(); i < tooltipSize; i++) {
            final var text = tooltip.get(i);
            context.drawString(client.font, text, context.guiWidth() / 2 - client.font.width(text) / 2, context.guiHeight() / 2 + 15 + i * 10, 0xFFFFFF, true);
        }
    }

}
