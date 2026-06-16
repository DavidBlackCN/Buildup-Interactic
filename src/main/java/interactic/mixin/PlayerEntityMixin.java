package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.InteracticItemExtensions;
import interactic.util.InteracticPlayerExtension;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerEntityMixin implements InteracticPlayerExtension {

    @Unique
    private float dropPower = 1;

    @Unique
    private boolean forcePickup;

    @Override
    public void setDropPower(float power) {
        this.dropPower = power;
    }

    @Override
    public void setForcePickup(boolean forcePickup) {
        this.forcePickup = forcePickup;
    }

    @Override
    public boolean isForcePickup() {
        return this.forcePickup;
    }

    @Inject(method = "drop", at = @At("RETURN"))
    private void applyDropPower(ItemStack stack, boolean throwRandomly, CallbackInfoReturnable<ItemEntity> cir) {
        if (!InteracticInit.getConfig().itemThrowing()) return;
        if (this.dropPower <= 1) return;

        var item = cir.getReturnValue();
        if (item == null) return;

        var velocity = ((Player)(Object)this).getViewVector(0f).scale(this.dropPower * .35f);
        item.setDeltaMovement(velocity);
        item.setPos(item.getX(), ((Player) (Object) this).getEyeY(), item.getZ());

        ((InteracticItemExtensions) item).markThrown();
        if (this.dropPower >= 5) ((InteracticItemExtensions) item).markFullPower();

        this.dropPower = 1;
    }
}
