package interactic.util;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import interactic.InteracticInit;
import net.fabricmc.loader.api.FabricLoader;

public class InteracticConfig {

    public static final ConfigClassHandler<InteracticConfig> HANDLER = ConfigClassHandler.createBuilder(InteracticConfig.class)
            .id(InteracticInit.id("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("interactic.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Disable every feature that requires Interactic on the server")
    public boolean clientOnlyMode = false;

    @SerialEntry(comment = "Whether players can pick up items by clicking them. This also affects the keybind")
    public boolean rightClickPickup = true;

    @SerialEntry(comment = "Whether players can throw items farther than normal by holding down the drop key")
    public boolean itemThrowing = true;

    @SerialEntry(comment = "Whether the item filter system should be active")
    public boolean itemFilterEnabled = true;

    @SerialEntry(comment = "Whether sneaking lets players ignore the filter and pick up items anyway")
    public boolean filterSneakOverride = true;

    @SerialEntry(comment = "Whether items that have damage modifiers should also deal damage when thrown")
    public boolean itemsActAsProjectiles = true;

    @SerialEntry(comment = "Whether players should be able to pick up items like normal")
    public boolean autoPickup = true;

    @SerialEntry(comment = "Whether Interactic should override Minecraft's default item rendering")
    public boolean fancyItemRendering = true;

    @SerialEntry(comment = "Whether Interactic should render the tooltips of items under the crosshair")
    public boolean renderItemTooltips = true;

    @SerialEntry(comment = "Whether Interactic should render the full tooltip of items")
    public boolean renderFullTooltip = false;

    @SerialEntry(comment = "Whether your arms should swing when dropping items")
    public boolean swingArm = true;

    @SerialEntry(comment = "Whether block items should lay flat on the ground")
    public boolean blocksLayFlat = false;

    public static InteracticConfig createAndLoad() {
        HANDLER.load();
        var config = HANDLER.instance();
        if (config.enforceClientOnlyMode()) HANDLER.save();
        return config;
    }

    public void save() {
        enforceClientOnlyMode();
        HANDLER.save();
    }

    private boolean enforceClientOnlyMode() {
        if (!clientOnlyMode) return false;

        boolean changed = itemsActAsProjectiles || itemThrowing || itemFilterEnabled || !autoPickup || rightClickPickup;
        itemsActAsProjectiles = false;
        itemThrowing = false;
        itemFilterEnabled = false;
        autoPickup = true;
        rightClickPickup = false;
        return changed;
    }

    public boolean clientOnlyMode() { return clientOnlyMode; }
    public void clientOnlyMode(boolean value) { clientOnlyMode = value; if (value) enforceClientOnlyMode(); }
    public boolean rightClickPickup() { return rightClickPickup; }
    public void rightClickPickup(boolean value) { rightClickPickup = clientOnlyMode ? false : value; }
    public boolean itemThrowing() { return itemThrowing; }
    public void itemThrowing(boolean value) { itemThrowing = clientOnlyMode ? false : value; }
    public boolean itemFilterEnabled() { return itemFilterEnabled; }
    public void itemFilterEnabled(boolean value) { itemFilterEnabled = clientOnlyMode ? false : value; }
    public boolean filterSneakOverride() { return filterSneakOverride; }
    public void filterSneakOverride(boolean value) { filterSneakOverride = value; }
    public boolean itemsActAsProjectiles() { return itemsActAsProjectiles; }
    public void itemsActAsProjectiles(boolean value) { itemsActAsProjectiles = clientOnlyMode ? false : value; }
    public boolean autoPickup() { return autoPickup; }
    public void autoPickup(boolean value) { autoPickup = clientOnlyMode || value; }
    public boolean fancyItemRendering() { return fancyItemRendering; }
    public void fancyItemRendering(boolean value) { fancyItemRendering = value; }
    public boolean renderItemTooltips() { return renderItemTooltips; }
    public void renderItemTooltips(boolean value) { renderItemTooltips = value; }
    public boolean renderFullTooltip() { return renderFullTooltip; }
    public void renderFullTooltip(boolean value) { renderFullTooltip = value; }
    public boolean swingArm() { return swingArm; }
    public void swingArm(boolean value) { swingArm = value; }
    public boolean blocksLayFlat() { return blocksLayFlat; }
    public void blocksLayFlat(boolean value) { blocksLayFlat = value; }
}
