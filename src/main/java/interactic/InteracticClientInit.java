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

    // Opens the item filter screen; intentionally unbound by default
    public static final KeyMapping OPEN_FILTER = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.interactic.open_filter",
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));

    // Quickly toggles the held item in the filter list; bound to "i" by default
    public static final KeyMapping QUICK_ADD = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.interactic.quick_add",
            InputConstants.Type.KEYSYM, InputConstants.KEY_I, KeyMapping.Category.MISC));

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

            while (OPEN_FILTER.consumeClick()) {
                if (!InteracticInit.getConfig().itemFilterEnabled()) continue;
                if (client.player == null) continue;
                InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.OpenFilterScreen());
            }

            while (QUICK_ADD.consumeClick()) {
                if (!InteracticInit.getConfig().itemFilterEnabled()) continue;

                var player = client.player;
                if (player == null) continue;
                if (player.getMainHandItem().isEmpty()) continue;

                InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.QuickAddItem());
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

        // Use the same targeting rule as pickup (strict on the ground, loose in the air)
        final var item = Helpers.raycastItem(camera, (float) player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), true);
        if (item == null || item.getItem().isEmpty()) return;

        var text = item.getItem().getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL).get(0);
        context.drawString(client.font, text, context.guiWidth() / 2 - client.font.width(text) / 2, context.guiHeight() / 2 + 15, 0xFFFFFFFF, true);
    }
}
