package monster.psyop.client.mixin.norender;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.render.NoRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconRenderer.class)
public class BeaconRendererMixin {
    @Inject(method = "renderBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/resources/ResourceLocation;FFJIIIFF)V", at = @At("HEAD"), cancellable = true)
    private static void renderBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, ResourceLocation resourceLocation, float f, float g, long l, int i, int j, int k, float h, float m, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(NoRender.class)) {
            NoRender module = Psyop.MODULES.get(NoRender.class);

            if (module.beams.get()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public <T extends BlockEntity> void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Vec3 vec3, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(NoRender.class)) {
            NoRender module = Psyop.MODULES.get(NoRender.class);

            if (module.beams.get()) {
                ci.cancel();
            }
        }
    }
}
