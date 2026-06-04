package interactic;

import com.mojang.serialization.Codec;
import interactic.util.InteracticNetworking;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemFilterItem extends Item {

    static {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(InteracticInit.getItemFilter());
        });
    }

    public static final DataComponentType<Boolean> ENABLED = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            InteracticInit.id("item_filter_enabled"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    public static final DataComponentType<Boolean> BLOCK_MODE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            InteracticInit.id("item_filter_block_mode"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    public static final DataComponentType<NonNullList<ItemStack>> FILTER_SLOTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            InteracticInit.id("item_filter_slots"),
            DataComponentType.<NonNullList<ItemStack>>builder()
                    .persistent(CodecUtils.toCodec(InventoryEntry.INVENTORY_ENDEC))
                    .networkSynchronized(CodecUtils.toPacketCodec(InventoryEntry.INVENTORY_ENDEC))
                    .build()
    );

    public ItemFilterItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(ENABLED, true)
                .component(BLOCK_MODE, true)
                .component(FILTER_SLOTS, NonNullList.withSize(ItemFilterScreenHandler.SLOT_COUNT, ItemStack.EMPTY)));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        final var playerStack = user.getItemInHand(hand);
        if (user.isShiftKeyDown()) {
            playerStack.update(ENABLED, false, enabled -> !enabled);
        } else {
            if (world.isClientSide()) return InteractionResult.SUCCESS;
            final var inv = new FilterInventory(playerStack);
            final var factory = new MenuProvider() {
                @Override
                public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player player) {
                    return new ItemFilterScreenHandler(syncId, playerInv, inv);
                }

                @Override
                public net.minecraft.network.chat.Component getDisplayName() {
                    return getName();
                }
            };
            user.openMenu(factory);
            InteracticNetworking.CHANNEL.serverHandle(user).send(new SetFilterModePacket(inv.getFilterMode()));
        }
        return InteractionResult.SUCCESS;
    }

    public static List<Item> getItemsInFilter(ItemStack stack) {
        return stack.getOrDefault(FILTER_SLOTS, NonNullList.<ItemStack>create()).stream().map(ItemStack::getItem).toList();
    }

    public static class FilterInventory extends SimpleContainer {

        public final ItemStack filter;
        private final NonNullList<ItemStack> items = NonNullList.withSize(ItemFilterScreenHandler.SLOT_COUNT, ItemStack.EMPTY);

        public FilterInventory(ItemStack filter) {
            super(ItemFilterScreenHandler.SLOT_COUNT);
            this.filter = filter;

            var filterItems = filter.getOrDefault(FILTER_SLOTS, this.items);
            for (int i = 0; i < filterItems.size(); i++) {
                this.items.set(i, filterItems.get(i));
            }
        }

        public void setFilterMode(boolean mode) {
            this.filter.set(BLOCK_MODE, mode);
        }

        public boolean getFilterMode() {
            return this.filter.getOrDefault(BLOCK_MODE, false);
        }

        @Override
        public int getContainerSize() {
            return ItemFilterScreenHandler.SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            return this.items.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return this.items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            var stack = this.items.get(slot).copy();
            this.items.set(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            var stack = this.items.get(slot).copy();
            this.items.set(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            this.items.set(slot, stack);
        }

        @Override
        public void setChanged() {
            this.filter.set(FILTER_SLOTS, this.items);
        }

        @Override
        public boolean stillValid(Player player) {
            return player.getInventory().contains(filter);
        }

        @Override
        public void clearContent() {
            Collections.fill(this.items, ItemStack.EMPTY);
        }
    }

    public record InventoryEntry(ItemStack stack, int slot) {
        private static final ReflectiveEndecBuilder BUILDER = new ReflectiveEndecBuilder(MinecraftEndecs::addDefaults);

        public static final Endec<InventoryEntry> ENDEC = RecordEndec.create(BUILDER, InventoryEntry.class);
        public static final Endec<NonNullList<ItemStack>> INVENTORY_ENDEC = InventoryEntry.ENDEC.listOf().xmap(
                entries -> {
                    var list = NonNullList.withSize(ItemFilterScreenHandler.SLOT_COUNT, ItemStack.EMPTY);
                    entries.forEach(entry -> list.set(entry.slot, entry.stack));
                    return list;
                }, stacks -> {
                    var entries = new ArrayList<InventoryEntry>();
                    for (int i = 0; i < stacks.size(); i++) {
                        if (stacks.get(i).isEmpty()) continue;
                        entries.add(new InventoryEntry(stacks.get(i), i));
                    }
                    return entries;
                }
        );
    }

    public record SetFilterModePacket(boolean mode) {}
}
