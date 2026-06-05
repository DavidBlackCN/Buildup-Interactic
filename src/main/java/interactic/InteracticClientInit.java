package interactic;

import interactic.util.Helpers;
import interactic.util.InteracticNetworking;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public class InteracticClientInit implements ClientModInitializer {

    public static final KeyMapping PICKUP_ITEM = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.interactic.pickup_item",
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));

    @Override
    public void onInitializeClient() {
        MenuScreens.register(InteracticInit.ITEM_FILTER_SCREEN_HANDLER, ItemFilterScreen::new);
        HudElementRegistry.addLast(InteracticInit.id("item_tooltip"), InteracticClientInit::renderItemTooltip);

        // Dedicated pickup keybinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (PICKUP_ITEM.consumeClick()) {
                if (!InteracticInit.getConfig().rightClickPickup()) continue;

                var player = client.player;
                var camera = client.getCameraEntity();
                if (player == null || camera == null) continue;

                var item = Helpers.raycastItem(camera, (float) player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE));
                if (item == null) continue;

                InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.Pickup(false));
                player.swing(InteractionHand.MAIN_HAND);
            }
        });

        ConfigScreenProviders.register("interactic", InteracticConfigScreen::new);
        InteracticNetworking.initClient();
    }

    private static void renderItemTooltip(GuiGraphics context, DeltaTracker tickCounter) {
        if (!InteracticInit.getConfig().renderItemTooltips()) return;

        final var client = Minecraft.getInstance();
        final var player = client.player;
        final var camera = client.getCameraEntity();
        if (player == null || camera == null) return;

        final var item = Helpers.raycastHoveredItem(camera, (float) player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), client.hitResult);
        if (item == null) return;

        var text = item.getItem().getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL).get(0);
        context.drawString(client.font, text, context.guiWidth() / 2 - client.font.width(text) / 2, context.guiHeight() / 2 + 15, 0xFFFFFF, true);
    }
}
