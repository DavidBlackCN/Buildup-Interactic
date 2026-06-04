package interactic.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class ItemDamageSource extends DamageSource {

    public ItemDamageSource(ItemEntity projectile, @Nullable Entity attacker) {
        super(projectile.level().damageSources().thrown(projectile, attacker).typeHolder(), projectile, attacker);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity entity) {
        Component attackerName = this.getEntity() == null ? this.getDirectEntity().getDisplayName() : this.getEntity().getDisplayName();
        ItemStack itemStack = ((ItemEntity) this.getDirectEntity()).getItem();
        String key = "death.attack.thrown_item";
        if (itemStack.is(Items.WOODEN_SWORD) || itemStack.is(Items.STONE_SWORD) || itemStack.is(Items.IRON_SWORD) ||
            itemStack.is(Items.GOLDEN_SWORD) || itemStack.is(Items.DIAMOND_SWORD) || itemStack.is(Items.NETHERITE_SWORD)) key = key + ".sword";
        if (itemStack.is(Items.WOODEN_AXE) || itemStack.is(Items.STONE_AXE) || itemStack.is(Items.IRON_AXE) ||
            itemStack.is(Items.GOLDEN_AXE) || itemStack.is(Items.DIAMOND_AXE) || itemStack.is(Items.NETHERITE_AXE)) key = key + ".axe";
        if (itemStack.is(Items.WOODEN_PICKAXE) || itemStack.is(Items.STONE_PICKAXE) || itemStack.is(Items.IRON_PICKAXE) ||
            itemStack.is(Items.GOLDEN_PICKAXE) || itemStack.is(Items.DIAMOND_PICKAXE) || itemStack.is(Items.NETHERITE_PICKAXE)) key = key + ".pickaxe";
        if (itemStack.is(Items.WOODEN_SHOVEL) || itemStack.is(Items.STONE_SHOVEL) || itemStack.is(Items.IRON_SHOVEL) ||
            itemStack.is(Items.GOLDEN_SHOVEL) || itemStack.is(Items.DIAMOND_SHOVEL) || itemStack.is(Items.NETHERITE_SHOVEL)) key = key + ".shovel";
        if (itemStack.is(Items.WOODEN_HOE) || itemStack.is(Items.STONE_HOE) || itemStack.is(Items.IRON_HOE) ||
            itemStack.is(Items.GOLDEN_HOE) || itemStack.is(Items.DIAMOND_HOE) || itemStack.is(Items.NETHERITE_HOE)) key = key + ".hoe";
        return Component.translatable(key, entity.getDisplayName(), attackerName, itemStack.getDisplayName());
    }
}
