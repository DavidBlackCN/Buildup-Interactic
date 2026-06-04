package interactic.util;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface InteracticItemRenderStateExtensions {
    @Nullable
    ItemEntity interactic$getItemEntity();

    void interactic$setItemEntity(@Nullable ItemEntity entity);

    ItemStack interactic$getItemStack();

    void interactic$setItemStack(ItemStack itemStack);
}
