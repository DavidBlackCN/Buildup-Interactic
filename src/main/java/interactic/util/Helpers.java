package interactic.util;

import interactic.InteracticInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Helpers {

    public static ItemEntity raycastItem(Entity camera, float reach) {
        return raycastItem(camera, reach, false);
    }

    public static ItemEntity raycastItem(Entity camera, float reach, boolean strict) {
        if (camera == null) return null;

        Vec3 start = camera.getEyePosition();
        Vec3 normalizedFacing = camera.getViewVector(1.0F);
        Vec3 end = start.add(normalizedFacing.scale(reach));

        final EntityHitResult result = ProjectileUtil.getEntityHitResult(camera, start, end,
                camera.getBoundingBox().expandTowards(normalizedFacing.scale(reach)).inflate(1), entity -> entity instanceof ItemEntity, reach * reach);

        if (result == null || !(result.getEntity() instanceof ItemEntity item)) {
            return null;
        }

        // Strict targeting only applies to items resting on the ground, so that aiming near a
        // grounded item doesn't hijack right-click block placement. Airborne items (e.g. ones you
        // just threw) keep the generous pick-radius targeting so they can still be caught mid-flight.
        final boolean strictHere = strict && item.onGround();

        Vec3 itemHitLocation = result.getLocation();
        if (strictHere) {
            var strictHit = item.getBoundingBox().inflate(0.02).clip(start, end);
            if (strictHit.isEmpty()) {
                return null;
            }
            itemHitLocation = strictHit.get();
        }

        if (camera.pick(reach, 1f, false) instanceof BlockHitResult blockResult && blockResult.getType() == HitResult.Type.BLOCK) {
            if (!camera.level().getBlockState(blockResult.getBlockPos()).getCollisionShape(camera.level(), blockResult.getBlockPos()).isEmpty()) {
                double itemDistanceSq = start.distanceToSqr(itemHitLocation);
                double blockDistanceSq = start.distanceToSqr(blockResult.getLocation());
                if (strictHere ? blockDistanceSq <= itemDistanceSq + 1.0E-6 : blockDistanceSq + 0.16 < itemDistanceSq) {
                    return null;
                }
            }
        }

        return item;
    }

    public static boolean canPlayerPickUpItem(Player player, ItemEntity item) {
        // Explicit right-click pickup expresses clear intent and overrides every filter rule
        if (((InteracticPlayerExtension) player).isForcePickup()) return true;

        if (!InteracticInit.getConfig().autoPickup() && player.isShiftKeyDown() && !item.entityTags().contains("interactic.ignore_auto_pickup_rule")) {
            return true;
        }

        if (!InteracticInit.getConfig().itemFilterEnabled()) return true;

        // Sneaking ignores the filter entirely and picks the item up regardless
        if (InteracticInit.getConfig().filterSneakOverride() && player.isShiftKeyDown()) {
            return true;
        }

        final boolean listed = ItemFilter.contains(player, item.getItem().getItem());
        if (ItemFilter.blockMode(player)) {
            // Block mode: items in the list are NOT picked up, everything else is
            return !listed;
        } else {
            // Allow mode: only items in the list are picked up
            return listed;
        }
    }
}
