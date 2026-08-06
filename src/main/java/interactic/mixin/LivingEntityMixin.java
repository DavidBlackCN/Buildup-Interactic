package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.InteracticItemExtensions;
import interactic.util.InteracticPlayerExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    /**
     * Player.drop adds the item to the world inside LivingEntity.drop. Applying
     * throw velocity at RETURN is too late: the initial entity spawn packet may
     * already contain vanilla position and velocity. Prepare the item before it
     * is added so clients see the correct trajectory from its first frame.
     */
    @ModifyArg(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            index = 0
    )
    private Entity interactic$applyDropPowerBeforeSpawn(Entity entity) {
        if (!((Object) this instanceof Player player) || !(entity instanceof ItemEntity item)) return entity;

        float power = ((InteracticPlayerExtension) player).takeDropPower();
        if (!InteracticInit.getConfig().itemThrowing() || power <= 1) return entity;

        item.setDeltaMovement(player.getViewVector(0f).scale(power * .35f));
        item.setPos(item.getX(), player.getEyeY(), item.getZ());

        ((InteracticItemExtensions) item).markThrown();
        if (power >= 5) ((InteracticItemExtensions) item).markFullPower();
        return entity;
    }
}
