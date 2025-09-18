package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.render.Chams;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static monster.psyop.client.Liberty.MC;

@Mixin(value = BlockEntityRenderDispatcher.class, priority = 749)
public abstract class BlockEntityRenderDispatcherMixin {
    @Shadow
    private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> blockEntityRenderer, T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, Vec3 vec3) {
    }

    // Chams - Block Entity Glow Color
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;setupAndRender(Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/phys/Vec3;)V"))
    private <T extends BlockEntity> void setBlockEntityBufferSource(BlockEntityRenderer<T> blockEntityRenderer, T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, Vec3 vec3) {
        if (Liberty.MODULES.isActive(Chams.class)) {
            Chams module = Liberty.MODULES.get(Chams.class);

            if (module.glowBlockEntities.value().contains(blockEntity.getType())) {
                float[] glowColor = module.glowBlockEntities.colorMap.getOrDefault(blockEntity.getType(), module.blockEntityGlowColor.get());

                OutlineBufferSource outlineBufferSource = MC.renderBuffers().outlineBufferSource();
                outlineBufferSource.setColor((int) (glowColor[0] * 255), (int) (glowColor[1] * 255), (int) (glowColor[2] * 255), (int) (glowColor[3] * 255));

                setupAndRender(blockEntityRenderer, blockEntity, f, poseStack, outlineBufferSource, vec3);
                return;
            }
        }

        setupAndRender(blockEntityRenderer, blockEntity, f, poseStack, multiBufferSource, vec3);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z"))
    private <T extends BlockEntity> boolean shouldRender(BlockEntityRenderer<T> instance, T blockEntity, Vec3 vec3) {
        if (Liberty.MODULES.isActive(Chams.class)) {
            Chams module = Liberty.MODULES.get(Chams.class);

            if (module.glowBlockEntities.value().contains(blockEntity.getType()) || module.alwaysRenderBlockEntities.get()) {
                return true;
            }
        }

        return instance.shouldRender(blockEntity, vec3);
    }
}
