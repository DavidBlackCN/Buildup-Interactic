package interactic;

import interactic.util.InteracticNetworking;
import interactic.util.ItemFilter;
import io.wispforest.owo.client.screens.MenuUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ItemFilterScreenHandler extends AbstractContainerMenu {

    public static final int SLOT_COUNT = ItemFilter.SLOT_COUNT;
    private final SimpleContainer inventory;
    private final Player player;

    public ItemFilterScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new FilterInventory(playerInventory.player));
    }

    public ItemFilterScreenHandler(int syncId, Inventory playerInventory, SimpleContainer inventory) {
        super(InteracticInit.ITEM_FILTER_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        checkContainerSize(inventory, SLOT_COUNT);

        this.player = playerInventory.player;
        inventory.startOpen(player);

        SlotGenerator.begin(this::addSlot, 8, 20)
                .slotFactory(GhostSlot::new)
                .grid(inventory, 0, 9, 3)
                .defaultSlotFactory()
                .moveTo(8, 96)
                .playerInventory(playerInventory);
    }

    public void setFilterMode(boolean mode) {
        ItemFilter.setMode(player, mode);
        InteracticNetworking.CHANNEL.serverHandle(player).send(new InteracticNetworking.SetFilterModePacket(mode));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return MenuUtils.handleSlotTransfer(this, index, 0);
    }

    @Override
    public void removed(Player playerEntity) {
        super.removed(playerEntity);
        this.inventory.stopOpen(playerEntity);
    }

    /**
     * A container backed by the player's {@link ItemFilter} attachment. Reads the filter
     * contents on open and writes them back whenever a slot changes.
     */
    public static class FilterInventory extends SimpleContainer {

        private final Player player;

        public FilterInventory(Player player) {
            super(SLOT_COUNT);
            this.player = player;

            var items = ItemFilter.get(player).items();
            for (int i = 0; i < items.size() && i < SLOT_COUNT; i++) {
                super.setItem(i, items.get(i).copy());
            }
        }

        @Override
        public void setChanged() {
            super.setChanged();
            var items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
            for (int i = 0; i < SLOT_COUNT; i++) {
                items.set(i, getItem(i).copy());
            }
            ItemFilter.setItems(player, items);
        }
    }

    private static class GhostSlot extends Slot {

        public GhostSlot(net.minecraft.world.Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            this.set(new ItemStack(stack.getItem()));
            return false;
        }

        @Override
        public boolean mayPickup(Player playerEntity) {
            this.set(ItemStack.EMPTY);
            return false;
        }
    }
}
