package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.render.AntiBlinker;
import monster.psyop.client.impl.modules.render.HandView;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Redirect(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isModelPartShown(Lnet/minecraft/world/entity/player/PlayerModelPart;)Z"))
    public boolean showModelParts(AbstractClientPlayer instance, PlayerModelPart playerModelPart) {
        if (Psyop.MODULES.isActive(AntiBlinker.class)) {
            return true;
        }

        return instance.isModelPartShown(playerModelPart);
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType modifyHandBuffer(ResourceLocation resourceLocation) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.wizard.get()) {
                return CoreRendering.entityTranslucent(resourceLocation, true);
            }
        }
        return RenderType.entityTranslucent(resourceLocation, true);
    }

    @Inject(method = "renderHand", at = @At("TAIL"))
    public void renderHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation, ModelPart modelPart, boolean bl, CallbackInfo ci) {

        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.wizard.get()) {
                modelPart.render(poseStack, multiBufferSource.getBuffer(RenderType.outline(resourceLocation)), i, OverlayTexture.NO_OVERLAY, ImColorW.toInt(module.wizardColor.get()));
            }
        }
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    public void modifyHandColor(ModelPart instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.modifyColor.get()) {
                instance.render(poseStack, vertexConsumer, i, j, ImColorW.toInt(module.color.get()));
                return;
            }

        }

        instance.render(poseStack, vertexConsumer, i, j);
    }
}
