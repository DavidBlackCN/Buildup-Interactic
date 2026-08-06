package interactic.mixin;

import interactic.util.InteracticPlayerExtension;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

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
    public float takeDropPower() {
        float power = this.dropPower;
        this.dropPower = 1;
        return power;
    }

    @Override
    public void setForcePickup(boolean forcePickup) {
        this.forcePickup = forcePickup;
    }

    @Override
    public boolean isForcePickup() {
        return this.forcePickup;
    }

}
