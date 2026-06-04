package interactic.mixin;

import interactic.util.InteracticItemRenderStateExtensions;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
public class ItemEntityRenderStateMixin implements InteracticItemRenderStateExtensions {

    @Unique
    @Nullable
    private ItemEntity interactic$itemEntity;

    @Unique
    private ItemStack interactic$itemStack = ItemStack.EMPTY;

    @Override
    @Nullable
    public ItemEntity interactic$getItemEntity() {
        return this.interactic$itemEntity;
    }

    @Override
    public void interactic$setItemEntity(@Nullable ItemEntity entity) {
        this.interactic$itemEntity = entity;
    }

    @Override
    public ItemStack interactic$getItemStack() {
        return this.interactic$itemStack;
    }

    @Override
    public void interactic$setItemStack(ItemStack itemStack) {
        this.interactic$itemStack = itemStack;
    }
}
