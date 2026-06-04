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
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Helpers {

    public static ItemEntity raycastItem(Entity camera, float reach) {
        Vec3 normalizedFacing = camera.getViewVector(1.0F);
        Vec3 denormalizedFacing = camera.getEyePosition().add(normalizedFacing.x * reach, normalizedFacing.y * reach, normalizedFacing.z * reach);

        final EntityHitResult result = ProjectileUtil.getEntityHitResult(camera, camera.getEyePosition(), denormalizedFacing,
                camera.getBoundingBox().expandTowards(normalizedFacing.scale(reach)).inflate(1), entity -> entity instanceof ItemEntity, reach * reach);

        if (result == null || !(result.getEntity() instanceof ItemEntity item)) {
            return null;
        }

        var distance = camera.position().distanceTo(result.getLocation()) - .3;
        if (camera.pick(distance, 1f, false) instanceof BlockHitResult blockResult) {
            if (!camera.level().getBlockState(blockResult.getBlockPos()).getCollisionShape(camera.level(), blockResult.getBlockPos()).isEmpty()) {
                return null;
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
