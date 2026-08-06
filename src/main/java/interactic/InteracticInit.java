package interactic;

import interactic.util.InteracticConfig;
import interactic.util.InteracticNetworking;
import interactic.util.ItemFilter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class InteracticInit implements ModInitializer {

    public static final String MOD_ID = "interactic";

    private static final InteracticConfig CONFIG = InteracticConfig.createAndLoad();
    private static float itemRotationSpeedMultiplier = 1f;

    public static final MenuType<ItemFilterScreenHandler> ITEM_FILTER_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU, id("item_filter"), new MenuType<>(ItemFilterScreenHandler::new, FeatureFlags.DEFAULT_FLAGS));

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("iris")) itemRotationSpeedMultiplier = 0.5f;

        // Touch ItemFilter to ensure its attachment type is registered during init
        ItemFilter.touch();

        InteracticNetworking.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static float getItemRotationSpeedMultiplier() {
        return itemRotationSpeedMultiplier;
    }

    public static InteracticConfig getConfig() {
        return CONFIG;
    }
}
