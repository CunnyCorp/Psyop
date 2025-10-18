package monster.psyop.client.mixin;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.modules.combat.KillAura;
import monster.psyop.client.impl.modules.render.BlockLights;
import monster.psyop.client.impl.modules.render.Chams;
import monster.psyop.client.impl.modules.render.RenderTweaks;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 749)
public class LevelRendererMixin {
    @Inject(method = "renderBlockEntities", at = @At("TAIL"))
    private void psyop$renderEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MultiBufferSource.BufferSource bufferSource2, Camera camera, float f, CallbackInfo ci) {
        GlStateManager._depthMask(false);
        GlStateManager._disableDepthTest();

        var buffers = Psyop.MC.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(CoreRendering.lines());
        VertexConsumer quads = buffers.getBuffer(RenderTweaks.getQuadsRenderType());
        PoseStack stack = new PoseStack();
        RenderSystem.lineWidth(5.0f);

        Psyop.EVENT_HANDLER.call(OnRender.get(lines, quads, stack));

        buffers.endBatch(CoreRendering.lines());
        buffers.endBatch(CoreRendering.quads());

        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(true);
    }

    @Unique
    private EntityType<?> lastGlowingEntityType;
    @Unique
    private Entity lastGlowingEntity;


    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V"))
    public void setColor0(OutlineBufferSource instance, int i, int j, int k, int l) {
        if (Psyop.MODULES.isActive(KillAura.class)) {
            KillAura module = Psyop.MODULES.get(KillAura.class);

            if (module.shouldGlow.get()) {
                if (module.target == lastGlowingEntity) {
                    float[] glowColor = module.glowColor.get();
                    instance.setColor((int) (glowColor[0] * 255), (int) (glowColor[1] * 255), (int) (glowColor[2] * 255), (int) (glowColor[3] * 255));
                    return;
                }
            }
        }


        if (Psyop.MODULES.isActive(Chams.class)) {
            Chams module = Psyop.MODULES.get(Chams.class);
            if (!module.toggleGlow.get() && module.glowEntities.value().contains(lastGlowingEntityType)) {
                float[] glowColor = module.glowEntities.colorMap.getOrDefault(lastGlowingEntityType, module.entityGlowColor.get());
                instance.setColor((int) (glowColor[0] * 255), (int) (glowColor[1] * 255), (int) (glowColor[2] * 255), (int) (glowColor[3] * 255));
                return;
            }
        }

        instance.setColor(i, j, k, l);
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
    public boolean shouldEntityAppearGlowing0(Minecraft instance, Entity entity) {
        lastGlowingEntityType = entity.getType();
        lastGlowingEntity = entity;


        if (Psyop.MODULES.isActive(Chams.class)) {
            Chams module = Psyop.MODULES.get(Chams.class);
            if (!module.toggleGlow.get() && module.glowEntities.value().contains(entity.getType())) {
                return true;
            }
        }

        if (Psyop.MODULES.isActive(KillAura.class)) {
            KillAura module = Psyop.MODULES.get(KillAura.class);

            if (module.shouldGlow.get() && module.target == entity) {
                return true;
            }
        }

        return instance.shouldEntityAppearGlowing(entity);
    }

    // BlockLights - 0
    @Redirect(method = "getLightColor(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private static int getLightColor0(BlockState instance) {
        if (Psyop.MODULES.isActive(BlockLights.class)) {
            BlockLights module = Psyop.MODULES.get(BlockLights.class);
            if (module.blockList.value().contains(instance.getBlock())) {
                return module.light.get();
            }
        }

        return instance.getLightEmission();
    }

    // BlockLights - 1
    @Redirect(method = "getLightColor(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;emissiveRendering(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean getLightColor0(BlockState instance, BlockGetter blockGetter, BlockPos blockPos) {
        if (Psyop.MODULES.isActive(BlockLights.class)) {
            BlockLights module = Psyop.MODULES.get(BlockLights.class);
            if (module.blockList.value().contains(instance.getBlock())) {
                return true;
            }
        }

        return instance.emissiveRendering(blockGetter, blockPos);
    }

}
