package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.client.RenderTweaks;
import monster.psyop.client.impl.modules.render.Chams;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    protected M model;
    @Unique
    LivingEntityRenderState renderState;

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    public void onRender(LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        this.renderState = renderState;
    }

    @Redirect(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    public void penisLol(EntityModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
        if (Psyop.MODULES.isActive(Chams.class)) {
            Chams module = Psyop.MODULES.get(Chams.class);

            if (module.walls.get() && instance instanceof PlayerModel) {
                return;
            }
        }

        instance.renderToBuffer(poseStack, vertexConsumer, i, j, k);
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", shift = At.Shift.AFTER))
    public void injectAfterRender(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(Chams.class)) {
            Chams module = Psyop.MODULES.get(Chams.class);

            if (module.walls.get() && livingEntityRenderState instanceof PlayerRenderState state) {
                int color;
                int color2;
                int color3;

                if (FriendManager.roles.containsKey(state.name)) {
                    color = ImColorW.toInt(module.layerOneFriendColor.get());
                    color2 = ImColorW.toInt(module.layerTwoFriendColor.get());
                    color3 = ImColorW.toInt(module.layerThreeFriendColor.get());
                } else {
                    color = ImColorW.toInt(module.layerOneColor.get());
                    color2 = ImColorW.toInt(module.layerTwoColor.get());
                    color3 = ImColorW.toInt(module.layerThreeColor.get());
                }

                if (!module.layerOneMode.get().equals("None")) {
                    RenderType renderType = switch (module.layerOneMode.get()) {
                        case "Quads" -> RenderTweaks.getQuadsRenderType();
                        case "Outline" -> RenderType.outline(state.skin.texture());
                        default -> CoreRendering.entityTranslucent(state.skin.texture(), true);
                    };

                    this.model.renderToBuffer(poseStack, Psyop.MC.renderBuffers().bufferSource().getBuffer(renderType), i, OverlayTexture.NO_OVERLAY, color);
                }

                if (!module.layerTwoMode.get().equals("None")) {
                    RenderType renderType2 = switch (module.layerTwoMode.get()) {
                        case "Quads" -> RenderTweaks.getQuadsRenderType();
                        case "Outline" -> RenderType.outline(state.skin.texture());
                        default -> CoreRendering.entityTranslucent(state.skin.texture(), true);
                    };

                    this.model.renderToBuffer(poseStack, Psyop.MC.renderBuffers().bufferSource().getBuffer(renderType2), i, OverlayTexture.NO_OVERLAY, color2);
                }

                if (!module.layerThreeMode.get().equals("None")) {
                    RenderType renderType2 = switch (module.layerThreeMode.get()) {
                        case "Quads" -> RenderTweaks.getQuadsRenderType();
                        case "Outline" -> RenderType.outline(state.skin.texture());
                        default -> CoreRendering.entityTranslucent(state.skin.texture(), true);
                    };

                    this.model.renderToBuffer(poseStack, Psyop.MC.renderBuffers().bufferSource().getBuffer(renderType2), i, OverlayTexture.NO_OVERLAY, color3);
                }
            }
        }
    }
}
