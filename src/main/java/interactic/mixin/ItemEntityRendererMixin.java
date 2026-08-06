package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.InteracticItemExtensions;
import interactic.util.InteracticItemRenderStateExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.shapes.CollisionContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity, ItemEntityRenderState> {

    private static final double TWO_PI = Math.PI * 2;
    private static final double HALF_PI = Math.PI * 0.5;
    private static final double THREE_HALF_PI = Math.PI * 1.5;

    @Shadow @Final private RandomSource random;

    private ItemEntityRendererMixin(EntityRendererProvider.Context dispatcher) {
        super(dispatcher);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstructor(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.shadowRadius = 0F;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void captureEntity(ItemEntity entity, ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
        var interacticState = (InteracticItemRenderStateExtensions) state;
        interacticState.interactic$setItemEntity(entity);
        interacticState.interactic$setItemStack(entity.getItem().copy());
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void render(ItemEntityRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        if (!InteracticInit.getConfig().fancyItemRendering()) return;

        var interacticState = (InteracticItemRenderStateExtensions) state;
        ItemEntity entity = interacticState.interactic$getItemEntity();
        if (entity == null) return;

        ItemStack itemStack = interacticState.interactic$getItemStack();
        if (itemStack.isEmpty()) return;

        ci.cancel();

        int seed = itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) * entity.getId();
        this.random.setSeed(seed);

        matrices.pushPose();

        final var item = itemStack.getItem();
        // Treat items whose ground model is a flat 2D sprite as lay-flat items, regardless of whether
        // they are BlockItems. This matches vanilla's own 3D-vs-flat test (model Z depth > 0.0625).
        // Fixes modded seeds/crops that are BlockItems but render as flat sprites standing upright.
        boolean flatSpriteModel = state.item.getModelBoundingBox().getZsize() <= 0.0625;
        boolean generatedFlatBlockItem = flatSpriteModel
                || itemStack.is(Items.STRING) || itemStack.is(Items.WHEAT_SEEDS) || itemStack.is(Items.BEETROOT_SEEDS);
        double blockHeight = 0;
        boolean treatAsDepthModel = false;
        boolean isFlatModel = false;
        boolean isFlatNonDepthBlock = false;
        if (item instanceof BlockItem blockItem && !generatedFlatBlockItem) {
            final var blockState = blockItem.getBlock().defaultBlockState();
            final var shape = blockState.getShape(entity.level(), entity.blockPosition(), CollisionContext.empty());
            if (!shape.isEmpty()) {
                blockHeight = shape.max(Direction.Axis.Y);
                final double blockWidth = shape.max(Direction.Axis.X) - shape.min(Direction.Axis.X);
                final double blockDepth = shape.max(Direction.Axis.Z) - shape.min(Direction.Axis.Z);
                treatAsDepthModel = blockState.isCollisionShapeFullBlock(entity.level(), entity.blockPosition());
                isFlatModel = blockHeight <= 0.75 && blockWidth >= 0.75 && blockDepth >= 0.75;
                isFlatNonDepthBlock = isFlatModel && !treatAsDepthModel;
            }
        }
        final double distanceToCenter = (0.5 - blockHeight + blockHeight / 2) * 0.25;

        matrices.translate(0, 0.125f, 0);
        if (treatAsDepthModel) matrices.translate(0, distanceToCenter, 0);

        float groundDistance = isFlatNonDepthBlock ? 0.1875f : (treatAsDepthModel ? (float) distanceToCenter : (float) (0.125 - 0.0625));
        matrices.translate(0, -groundDistance, 0);

        matrices.translate(0, (random.nextDouble() - 0.5) * 0.005, 0);
        if (treatAsDepthModel && !isFlatModel) matrices.translate(0, -.1, 0);

        matrices.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));

        InteracticItemExtensions rotator = (InteracticItemExtensions) entity;
        if (rotator.getRotation() == -1) rotator.setRotation((random.nextInt(20) - 10) * 0.15f);

        float angle;
        if (entity.onGround()) {
            angle = rotator.getRotation();
        } else {
            angle = (float) (rotator.getRotation()
                    + Mth.clamp(entity.getDeltaMovement().y * 0.25, 0.075, 0.3)
                    * (entity.isInWater() ? 0.25f : 1)
                    * (Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks() * 5)
                    * InteracticInit.getItemRotationSpeedMultiplier());
        }

        if (angle >= TWO_PI) angle -= TWO_PI;

        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        if (entity.onGround() && !(angle == 0 || angle == (float) Math.PI)) {
            if (angle > Math.PI) {
                if (angle > THREE_HALF_PI) angle += tickDelta * 0.5f;
                else angle -= tickDelta * 0.5f;
            } else {
                if (angle > HALF_PI) {
                    angle += tickDelta * 0.5f;
                    if (angle > Math.PI) angle = (float) Math.PI;
                } else angle -= tickDelta * 0.5f;
            }
            if (angle < 0) angle = 0;
            if (angle > TWO_PI) angle = 0;
        }

        if (entity.onGround() && isFlatNonDepthBlock) {
            angle = 0;
        }

        rotator.setRotation(angle);

        if (treatAsDepthModel) matrices.translate(0, -distanceToCenter, 0);
        matrices.mulPose(Axis.XP.rotation((float) (angle + (isFlatModel ? 0 : HALF_PI))));

        if (treatAsDepthModel && !isFlatModel && !InteracticInit.getConfig().blocksLayFlat()) {
            matrices.mulPose(Axis.YP.rotationDegrees(this.random.nextFloat() * 45));
            matrices.mulPose(Axis.ZP.rotationDegrees(this.random.nextFloat() * 45));
        }
        if (treatAsDepthModel) matrices.translate(0, distanceToCenter, 0);

        ItemEntityRenderer.submitMultipleFromCount(matrices, collector, state.lightCoords, state, this.random);

        matrices.popPose();
        super.submit(state, matrices, collector, cameraState);
    }
}
