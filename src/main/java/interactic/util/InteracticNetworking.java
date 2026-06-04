package interactic.util;

import interactic.InteracticInit;
import interactic.ItemFilterItem;
import interactic.ItemFilterScreen;
import interactic.ItemFilterScreenHandler;
import interactic.mixin.ItemEntityAccessor;
import interactic.mixin.PlayerInventoryAccessor;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class InteracticNetworking {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(InteracticInit.id("channel"));

    public static void init() {
        CHANNEL.registerClientboundDeferred(ItemFilterItem.SetFilterModePacket.class);


        CHANNEL.registerServerbound(Pickup.class, (message, access) -> {
            final var item = Helpers.raycastItem(access.player().getCamera(), 6);
            if (item == null || ((ItemEntityAccessor) item).interactic$getPickupDelay() == Short.MAX_VALUE) {
                return;
            }

            final var itemAccessor = (ItemEntityAccessor) item;
            final var pickupDelay = itemAccessor.interactic$getPickupDelay();
            itemAccessor.interactic$setPickupDelay(0);
            item.playerTouch(access.player());
            if (!item.isRemoved()) itemAccessor.interactic$setPickupDelay(pickupDelay);
        });


        CHANNEL.registerServerbound(DropWithPower.class, (message, access) -> {
            ((InteracticPlayerExtension) access.player()).setDropPower(message.power);

            var player = access.player();
            int selectedSlot = ((PlayerInventoryAccessor) (Object) player.getInventory()).interactic$getSelectedSlot();
            player.drop(player.getInventory().removeItem(selectedSlot, message.dropAll && !player.getInventory().getItem(selectedSlot).isEmpty() ? player.getInventory().getItem(selectedSlot).getCount() : 1), false);
        });

        CHANNEL.registerServerbound(FilterModeRequest.class, (message, access) -> {
            if (!(access.player().containerMenu instanceof ItemFilterScreenHandler filterHandler)) return;
            filterHandler.setFilterMode(message.newMode);
        });

    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        CHANNEL.registerClientbound(ItemFilterItem.SetFilterModePacket.class, (message, access) -> {
            if (!(access.runtime().screen instanceof ItemFilterScreen screen)) return;
            screen.blockMode = message.mode();
        });

    }

    public record Pickup() {}

    public record DropWithPower(float power, boolean dropAll) {}

    public record FilterModeRequest(boolean newMode) {}
}
