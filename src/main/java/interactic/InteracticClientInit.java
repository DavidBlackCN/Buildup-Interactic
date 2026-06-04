package interactic;

import interactic.util.InteracticNetworking;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.InteractionHand;

public class InteracticClientInit implements ClientModInitializer {

    public static final KeyMapping PICKUP_ITEM = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.interactic.pickup_item",
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));

    @Override
    public void onInitializeClient() {
        MenuScreens.register(InteracticInit.ITEM_FILTER_SCREEN_HANDLER, ItemFilterScreen::new);

        // Dedicated pickup keybinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (PICKUP_ITEM.consumeClick()) {
                InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.Pickup());
                client.player.swing(InteractionHand.MAIN_HAND);
            }
        });

        ConfigScreenProviders.register("interactic", InteracticConfigScreen::new);
        InteracticNetworking.initClient();
    }
}
