package interactic.util;

import interactic.InteracticInit;
import interactic.ItemFilterScreen;
import interactic.ItemFilterScreenHandler;
import interactic.mixin.ItemEntityAccessor;
import interactic.mixin.PlayerInventoryAccessor;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InteracticNetworking {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(InteracticInit.id("channel"));

    public static void init() {
        CHANNEL.registerClientboundDeferred(SetFilterModePacket.class);

        CHANNEL.registerServerbound(Pickup.class, (message, access) -> {
            if (!InteracticInit.getConfig().rightClickPickup()) return;

            var player = access.player();
            final var item = Helpers.raycastItem(player.getCamera(), (float) player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), message.strict());
            if (item == null || ((ItemEntityAccessor) item).interactic$getPickupDelay() == Short.MAX_VALUE) {
                return;
            }

            final var itemAccessor = (ItemEntityAccessor) item;
            final var pickupDelay = itemAccessor.interactic$getPickupDelay();
            itemAccessor.interactic$setPickupDelay(0);

            // Explicit right-click pickup is a deliberate action and should override the item filter
            ((InteracticPlayerExtension) player).setForcePickup(true);
            item.playerTouch(player);
            ((InteracticPlayerExtension) player).setForcePickup(false);

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

        CHANNEL.registerServerbound(OpenFilterScreen.class, (message, access) -> {
            if (!InteracticInit.getConfig().itemFilterEnabled()) return;
            openFilterScreen(access.player());
        });

        CHANNEL.registerServerbound(QuickAddItem.class, (message, access) -> {
            if (!InteracticInit.getConfig().itemFilterEnabled()) return;

            var player = access.player();
            var stack = player.getMainHandItem();
            if (stack.isEmpty()) return;

            var item = stack.getItem();
            var result = ItemFilter.toggleItem(player, item);
            var itemName = stack.getHoverName();

            switch (result) {
                case ADDED -> player.displayClientMessage(Component.translatable("message.interactic.filter_added", itemName), true);
                case REMOVED -> player.displayClientMessage(Component.translatable("message.interactic.filter_removed", itemName), true);
                case FULL -> player.displayClientMessage(Component.translatable("message.interactic.filter_full"), true);
            }
        });
    }

    public static void openFilterScreen(Player player) {
        final var factory = new MenuProvider() {
            @Override
            public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player p) {
                return new ItemFilterScreenHandler(syncId, playerInv, new ItemFilterScreenHandler.FilterInventory(p));
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("screen.interactic.item_filter");
            }
        };
        player.openMenu(factory);
        CHANNEL.serverHandle(player).send(new SetFilterModePacket(ItemFilter.blockMode(player)));
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        CHANNEL.registerClientbound(SetFilterModePacket.class, (message, access) -> {
            if (!(access.runtime().screen instanceof ItemFilterScreen screen)) return;
            screen.blockMode = message.mode();
        });
    }

    public record Pickup(boolean strict) {}

    public record DropWithPower(float power, boolean dropAll) {}

    public record FilterModeRequest(boolean newMode) {}

    public record OpenFilterScreen() {}

    public record QuickAddItem() {}

    public record SetFilterModePacket(boolean mode) {}
}
