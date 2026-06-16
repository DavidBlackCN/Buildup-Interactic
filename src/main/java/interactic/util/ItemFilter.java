package interactic.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import interactic.InteracticInit;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-player item filter, stored as a data attachment instead of a physical item.
 * A player has a single filter list plus a block/allow mode:
 * <ul>
 *     <li>block mode (default): items in the list are <i>not</i> picked up</li>
 *     <li>allow mode: only items in the list are picked up</li>
 * </ul>
 */
public final class ItemFilter {

    public static final int SLOT_COUNT = 27;

    public static final AttachmentType<FilterData> FILTER_DATA = AttachmentRegistry.create(
            InteracticInit.id("filter_data"),
            builder -> builder
                    .initializer(FilterData::createDefault)
                    .persistent(FilterData.CODEC)
                    .copyOnDeath()
    );

    private ItemFilter() {}

    /** No-op used to trigger class initialization so {@link #FILTER_DATA} gets registered. */
    public static void touch() {}

    public static FilterData get(Player player) {
        return player.getAttachedOrCreate(FILTER_DATA);
    }

    public static boolean blockMode(Player player) {
        return get(player).blockMode();
    }

    public static void setMode(Player player, boolean blockMode) {
        var data = get(player);
        player.setAttached(FILTER_DATA, new FilterData(blockMode, data.items()));
    }

    public static void setItems(Player player, NonNullList<ItemStack> items) {
        var data = get(player);
        player.setAttached(FILTER_DATA, new FilterData(data.blockMode(), copyOf(items)));
    }

    public static List<Item> getItems(Player player) {
        return get(player).items().stream().filter(stack -> !stack.isEmpty()).map(ItemStack::getItem).toList();
    }

    public static boolean contains(Player player, Item item) {
        for (var stack : get(player).items()) {
            if (!stack.isEmpty() && stack.is(item)) return true;
        }
        return false;
    }

    /**
     * Toggle an item in the filter list: remove it if present, otherwise add it to the
     * first empty slot.
     */
    public static ToggleResult toggleItem(Player player, Item item) {
        var data = get(player);
        var items = copyOf(data.items());

        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty() && items.get(i).is(item)) {
                items.set(i, ItemStack.EMPTY);
                player.setAttached(FILTER_DATA, new FilterData(data.blockMode(), items));
                return ToggleResult.REMOVED;
            }
        }

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, new ItemStack(item));
                player.setAttached(FILTER_DATA, new FilterData(data.blockMode(), items));
                return ToggleResult.ADDED;
            }
        }

        return ToggleResult.FULL;
    }

    private static NonNullList<ItemStack> copyOf(NonNullList<ItemStack> source) {
        var list = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < SLOT_COUNT && i < source.size(); i++) {
            list.set(i, source.get(i).copy());
        }
        return list;
    }

    public enum ToggleResult {
        ADDED, REMOVED, FULL
    }

    public record FilterData(boolean blockMode, NonNullList<ItemStack> items) {

        private static final Codec<NonNullList<ItemStack>> ITEMS_CODEC = Entry.CODEC.listOf().xmap(
                entries -> {
                    var list = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
                    for (var entry : entries) {
                        if (entry.slot() >= 0 && entry.slot() < SLOT_COUNT) list.set(entry.slot(), entry.stack());
                    }
                    return list;
                },
                list -> {
                    var entries = new ArrayList<Entry>();
                    for (int i = 0; i < list.size(); i++) {
                        if (!list.get(i).isEmpty()) entries.add(new Entry(i, list.get(i)));
                    }
                    return entries;
                }
        );

        public static final Codec<FilterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("block_mode", true).forGetter(FilterData::blockMode),
                ITEMS_CODEC.optionalFieldOf("items", NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY)).forGetter(FilterData::items)
        ).apply(instance, FilterData::new));

        public static FilterData createDefault() {
            return new FilterData(true, NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY));
        }

        private record Entry(int slot, ItemStack stack) {
            static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("slot").forGetter(Entry::slot),
                    ItemStack.CODEC.fieldOf("stack").forGetter(Entry::stack)
            ).apply(instance, Entry::new));
        }
    }
}
