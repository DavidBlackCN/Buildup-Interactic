package interactic.util;

public interface InteracticPlayerExtension {
    void setDropPower(float power);

    /**
     * Returns the pending throw power and resets it so it can only affect one drop.
     */
    float takeDropPower();

    /**
     * When set, the next {@link net.minecraft.world.entity.item.ItemEntity#playerTouch} call
     * bypasses the item filter. Used by explicit right-click pickup, which expresses clear
     * intent and should always win over the filter.
     */
    void setForcePickup(boolean forcePickup);

    boolean isForcePickup();
}
