package interactic;

import interactic.util.InteracticNetworking;
import io.wispforest.owo.client.screens.MenuUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ItemFilterScreenHandler extends AbstractContainerMenu {

    public static final int SLOT_COUNT = 27;
    private final SimpleContainer inventory;
    private final Player player;

    public ItemFilterScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(SLOT_COUNT));
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
        if (!(inventory instanceof ItemFilterItem.FilterInventory filterInventory)) return;
        filterInventory.setFilterMode(mode);

        InteracticNetworking.CHANNEL.serverHandle(player).send(new ItemFilterItem.SetFilterModePacket(mode));
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
