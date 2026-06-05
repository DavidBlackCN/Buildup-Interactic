package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.Helpers;
import interactic.util.InteracticItemExtensions;
import interactic.util.ItemDamageSource;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements InteracticItemExtensions {

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    private int age;

    @Shadow
    @Nullable
    public abstract Entity getOwner();

    @Unique
    private float rotation = -1;

    @Unique
    private boolean wasThrown;

    @Unique
    private boolean wasFullPower;

    private ItemEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public void markThrown() {
        this.wasThrown = true;
    }

    @Override
    public void markFullPower() {
        this.wasFullPower = true;
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;getItem()Lnet/minecraft/world/item/ItemStack;", ordinal = 0), cancellable = true)
    private void controlPickup(Player player, CallbackInfo ci) {
        if (Helpers.canPlayerPickUpItem(player, (ItemEntity) (Object) this)) return;
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dealThrowingDamage(CallbackInfo ci) {
        if (!InteracticInit.getConfig().itemsActAsProjectiles()) return;
        if (age < 2) return;

        var world = this.level();
        if (world.isClientSide()) return;

        if (this.onGround()) this.wasThrown = false;
        if (!this.wasThrown) return;

        final var hasDamageModifiers = this.getItem().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
                .modifiers().stream().anyMatch(entry -> entry.attribute().value() == Attributes.ATTACK_DAMAGE);
        if (!(this.wasFullPower || hasDamageModifiers)) return;

        var damage = new MutableDouble(2d);
        if (hasDamageModifiers) {
            this.getItem().forEachModifier(EquipmentSlot.MAINHAND, (attribEntry, modifier) -> {
                if (attribEntry.value() != Attributes.ATTACK_DAMAGE || modifier.operation() != AttributeModifier.Operation.ADD_VALUE) return;
                damage.add(modifier.amount());
            });
        }

        final var entities = world.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.15));
        if (entities.isEmpty()) return;

        final var target = entities.get(0);
        final var damageSource = new ItemDamageSource((ItemEntity) (Object) this, this.getOwner());

        if (target.hurtTime != 0 || target.isInvulnerable()) return;

        target.hurt(damageSource, damage.floatValue());
        this.getItem().hurtAndBreak(1, (ServerLevel) world, null, item -> this.discard());
    }

    @Override
    public float getPickRadius() {
        return this.getItem().getItem() instanceof BlockItem ? .45f : .2f;
    }
}
