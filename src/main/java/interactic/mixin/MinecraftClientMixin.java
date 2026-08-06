package interactic.mixin;

import interactic.InteracticClientInit;
import interactic.InteracticInit;
import interactic.util.Helpers;
import interactic.util.InteracticNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Unique
    private float dropPower = 0.9f;

    @Unique
    private boolean suppressUseUntilRelease;

    @Shadow
    @Final
    public Options options;

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void tryPickupItem(CallbackInfo ci) {
        if (suppressUseUntilRelease) {
            ci.cancel();
            return;
        }

        if (!InteracticInit.getConfig().rightClickPickup()) return;
        if (!InteracticClientInit.PICKUP_ITEM.isUnbound()) return;

        var player = Minecraft.getInstance().player;
        var camera = Minecraft.getInstance().getCameraEntity();
        if (player == null || camera == null) return;

        var item = Helpers.raycastItem(camera, (float) player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), true);
        if (item != null) {
            InteracticNetworking.sendPickup(true);
            suppressUseUntilRelease = true;
            player.swing(InteractionHand.MAIN_HAND);
            ci.cancel();
        }
    }

    @Inject(method = "handleKeybinds", at = @At("RETURN"))
    private void afterDrop(CallbackInfo ci) {
        if (!options.keyUse.isDown()) suppressUseUntilRelease = false;
        if (!InteracticInit.getConfig().itemThrowing()) return;

        if (dropPower > 0.9f && !options.keyDrop.isDown()) {
            long window = Minecraft.getInstance().getWindow().handle();
            final var dropAll = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;

            if (dropPower >= 1.5) {
                var player = Minecraft.getInstance().player;
                if (player == null) return;
                InteracticNetworking.sendDropWithPower(dropPower, dropAll);

                int selectedSlot = ((PlayerInventoryAccessor) (Object) player.getInventory()).interactic$getSelectedSlot();
                if (!player.getInventory().removeItem(selectedSlot, dropAll && !player.getInventory().getItem(selectedSlot).isEmpty() ? player.getInventory().getItem(selectedSlot).getCount() : 1).isEmpty()) {
                    if (InteracticInit.getConfig().swingArm()) player.swing(InteractionHand.MAIN_HAND);
                }
            } else {
                var player = Minecraft.getInstance().player;
                if (player != null && player.drop(dropAll)) {
                    if (InteracticInit.getConfig().swingArm()) player.swing(InteractionHand.MAIN_HAND);
                }
            }

            dropPower = 0.9f;
        }
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;drop(Z)Z"))
    private boolean handleDropPower(LocalPlayer clientPlayerEntity, boolean dropEntireStack) {
        if (!InteracticInit.getConfig().itemThrowing()) return clientPlayerEntity.drop(dropEntireStack);

        long window = Minecraft.getInstance().getWindow().handle();
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_PRESS) {
            dropPower += 0.075f;
            if (dropPower > 5) dropPower = 5;
            if (dropPower >= 1.5)
                clientPlayerEntity.sendOverlayMessage(Component.literal("Power: " + BigDecimal.valueOf(Math.max(dropPower, 1)).setScale(1, RoundingMode.HALF_UP)));
            return false;
        } else {
            return clientPlayerEntity.drop(dropEntireStack);
        }
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void dontSwingArms(LocalPlayer player, InteractionHand hand) {
        if (!InteracticInit.getConfig().swingArm()) return;
        player.swing(hand);
    }

}
