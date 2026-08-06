package interactic.util;

import interactic.InteracticInit;
import interactic.ItemFilterScreen;
import interactic.ItemFilterScreenHandler;
import interactic.mixin.ItemEntityAccessor;
import interactic.mixin.PlayerInventoryAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public final class InteracticNetworking {

    private InteracticNetworking() {}

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Pickup.TYPE, Pickup.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DropWithPower.TYPE, DropWithPower.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FilterModeRequest.TYPE, FilterModeRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(OpenFilterScreen.TYPE, OpenFilterScreen.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(QuickAddItem.TYPE, QuickAddItem.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SetFilterModePacket.TYPE, SetFilterModePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Pickup.TYPE, (message, context) -> {
            if (!InteracticInit.getConfig().rightClickPickup()) return;

            var player = context.player();
            var item = Helpers.raycastItem(player.getCamera(), (float) player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), message.strict());
            if (item == null || ((ItemEntityAccessor) item).interactic$getPickupDelay() == Short.MAX_VALUE) return;

            var itemAccessor = (ItemEntityAccessor) item;
            var pickupDelay = itemAccessor.interactic$getPickupDelay();
            itemAccessor.interactic$setPickupDelay(0);

            var extension = (InteracticPlayerExtension) player;
            extension.setForcePickup(true);
            try {
                item.playerTouch(player);
            } finally {
                extension.setForcePickup(false);
            }

            if (!item.isRemoved()) itemAccessor.interactic$setPickupDelay(pickupDelay);
        });

        ServerPlayNetworking.registerGlobalReceiver(DropWithPower.TYPE, (message, context) -> {
            var player = context.player();
            ((InteracticPlayerExtension) player).setDropPower(message.power());

            int selectedSlot = ((PlayerInventoryAccessor) (Object) player.getInventory()).interactic$getSelectedSlot();
            int count = message.dropAll() && !player.getInventory().getItem(selectedSlot).isEmpty()
                    ? player.getInventory().getItem(selectedSlot).getCount() : 1;
            player.drop(player.getInventory().removeItem(selectedSlot, count), false);
        });

        ServerPlayNetworking.registerGlobalReceiver(FilterModeRequest.TYPE, (message, context) -> {
            if (context.player().containerMenu instanceof ItemFilterScreenHandler filterHandler) {
                filterHandler.setFilterMode(message.newMode());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(OpenFilterScreen.TYPE, (message, context) -> {
            if (InteracticInit.getConfig().itemFilterEnabled()) openFilterScreen(context.player());
        });

        ServerPlayNetworking.registerGlobalReceiver(QuickAddItem.TYPE, (message, context) -> {
            if (!InteracticInit.getConfig().itemFilterEnabled()) return;

            var player = context.player();
            var stack = player.getMainHandItem();
            if (stack.isEmpty()) return;

            var result = ItemFilter.toggleItem(player, stack.getItem());
            var itemName = stack.getHoverName();
            switch (result) {
                case ADDED -> player.sendOverlayMessage(Component.translatable("message.interactic.filter_added", itemName));
                case REMOVED -> player.sendOverlayMessage(Component.translatable("message.interactic.filter_removed", itemName));
                case FULL -> player.sendOverlayMessage(Component.translatable("message.interactic.filter_full"));
            }
        });
    }

    public static void openFilterScreen(Player player) {
        var factory = new MenuProvider() {
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
        ServerPlayNetworking.send((net.minecraft.server.level.ServerPlayer) player,
                new SetFilterModePacket(ItemFilter.blockMode(player)));
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(SetFilterModePacket.TYPE, (message, context) -> {
            if (context.client().screen instanceof ItemFilterScreen screen) screen.blockMode = message.mode();
        });
    }

    @Environment(EnvType.CLIENT)
    public static void sendPickup(boolean strict) { ClientPlayNetworking.send(new Pickup(strict)); }
    @Environment(EnvType.CLIENT)
    public static void sendDropWithPower(float power, boolean dropAll) { ClientPlayNetworking.send(new DropWithPower(power, dropAll)); }
    @Environment(EnvType.CLIENT)
    public static void sendFilterMode(boolean mode) { ClientPlayNetworking.send(new FilterModeRequest(mode)); }
    @Environment(EnvType.CLIENT)
    public static void sendOpenFilterScreen() { ClientPlayNetworking.send(OpenFilterScreen.INSTANCE); }
    @Environment(EnvType.CLIENT)
    public static void sendQuickAddItem() { ClientPlayNetworking.send(QuickAddItem.INSTANCE); }

    public record Pickup(boolean strict) implements CustomPacketPayload {
        public static final Type<Pickup> TYPE = new Type<>(InteracticInit.id("pickup"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Pickup> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Pickup::strict, Pickup::new);
        @Override public Type<Pickup> type() { return TYPE; }
    }

    public record DropWithPower(float power, boolean dropAll) implements CustomPacketPayload {
        public static final Type<DropWithPower> TYPE = new Type<>(InteracticInit.id("drop_with_power"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DropWithPower> CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, DropWithPower::power, ByteBufCodecs.BOOL, DropWithPower::dropAll, DropWithPower::new);
        @Override public Type<DropWithPower> type() { return TYPE; }
    }

    public record FilterModeRequest(boolean newMode) implements CustomPacketPayload {
        public static final Type<FilterModeRequest> TYPE = new Type<>(InteracticInit.id("filter_mode"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FilterModeRequest> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, FilterModeRequest::newMode, FilterModeRequest::new);
        @Override public Type<FilterModeRequest> type() { return TYPE; }
    }

    public record OpenFilterScreen() implements CustomPacketPayload {
        public static final OpenFilterScreen INSTANCE = new OpenFilterScreen();
        public static final Type<OpenFilterScreen> TYPE = new Type<>(InteracticInit.id("open_filter_screen"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenFilterScreen> CODEC = StreamCodec.unit(INSTANCE);
        @Override public Type<OpenFilterScreen> type() { return TYPE; }
    }

    public record QuickAddItem() implements CustomPacketPayload {
        public static final QuickAddItem INSTANCE = new QuickAddItem();
        public static final Type<QuickAddItem> TYPE = new Type<>(InteracticInit.id("quick_add_item"));
        public static final StreamCodec<RegistryFriendlyByteBuf, QuickAddItem> CODEC = StreamCodec.unit(INSTANCE);
        @Override public Type<QuickAddItem> type() { return TYPE; }
    }

    public record SetFilterModePacket(boolean mode) implements CustomPacketPayload {
        public static final Type<SetFilterModePacket> TYPE = new Type<>(InteracticInit.id("set_filter_mode"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SetFilterModePacket> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, SetFilterModePacket::mode, SetFilterModePacket::new);
        @Override public Type<SetFilterModePacket> type() { return TYPE; }
    }
}
