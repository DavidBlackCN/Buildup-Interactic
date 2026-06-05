package interactic.util;

import interactic.InteracticInit;
import interactic.ItemFilterItem;
import interactic.mixin.PlayerInventoryAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

        Vec3 itemHitLocation = result.getLocation();
        if (strict) {
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
                if (strict ? blockDistanceSq <= itemDistanceSq + 1.0E-6 : blockDistanceSq + 0.16 < itemDistanceSq) {
                    return null;
                }
            }
        }

        return item;
    }

    public static boolean canPlayerPickUpItem(Player player, ItemEntity item) {
        if (!InteracticInit.getConfig().autoPickup() && player.isShiftKeyDown() && !item.getTags().contains("interactic.ignore_auto_pickup_rule")) {
            return true;
        }

        if (!InteracticInit.getConfig().itemFilterEnabled()) return true;
        var filters = ((PlayerInventoryAccessor) player.getInventory()).interactic$getItems().stream()
                .filter(stack -> stack.is(InteracticInit.getItemFilter()))
                .filter(stack -> stack.getOrDefault(ItemFilterItem.ENABLED, false))
                .map(stack -> new FilterEntry(stack, ItemFilterItem.getItemsInFilter(stack), stack.getOrDefault(ItemFilterItem.BLOCK_MODE, false)))
                .toList();

        if (filters.isEmpty()) return true;

        var allowed = filters.stream().allMatch(FilterEntry::blockMode);
        for (var entry : filters) {
            if (entry.blockMode) continue;

            if (entry.filterItems.contains(item.getItem().getItem())) {
                return true;
            }
        }

        if (!allowed) return false;

        for (var entry : filters) {
            if (!entry.blockMode) continue;

            if (entry.filterItems.contains(item.getItem().getItem())) {
                return false;
            }
        }

        return true;
    }

    private record FilterEntry(ItemStack filter, List<Item> filterItems, boolean blockMode) {}
}
