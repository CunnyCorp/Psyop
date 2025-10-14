package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.PsyopRenderTypes;
import monster.psyop.client.impl.modules.render.Chams;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Unique
    LivingEntityRenderState renderState;

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    public void onRender(LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        this.renderState = renderState;
    }

    @Redirect(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    public VertexConsumer redirectGetBuffer(MultiBufferSource instance, RenderType renderType) {
        if (Psyop.MODULES.isActive(Chams.class)) {
            Chams chams = Psyop.MODULES.get(Chams.class);

            if (chams.walls.value().contains(renderState.entityType)) {
                return instance.getBuffer(PsyopRenderTypes.entityTranslucent(((PlayerRenderState) renderState).skin.texture(), true));
            }
        }

        return instance.getBuffer(renderType);
    }
}
