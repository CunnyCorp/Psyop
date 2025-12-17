package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.render.ArmorView;
import monster.psyop.client.impl.modules.render.Chams;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static monster.psyop.client.Psyop.MC;

@Mixin(value = CapeLayer.class, priority = 749)
public class CapeLayerMixin {
    @Unique
    private PlayerRenderState state = null;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "HEAD"))
    public void renderStoreData(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState playerRenderState, float f, float g, CallbackInfo ci) {
        state = playerRenderState;
    }

    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    public void render(HumanoidModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
        int color = -1;

        if (Psyop.MODULES.isActive(ArmorView.class)) {
            ArmorView module = Psyop.MODULES.get(ArmorView.class);

            if (module.modifyCapeColor.get()) {
                color = ImColorW.toInt(module.capeColor.get());
            }
        }

        instance.renderToBuffer(poseStack, vertexConsumer, i, j, color);

        if (Psyop.MODULES.isActive(Chams.class)) {
            Chams module = Psyop.MODULES.get(Chams.class);

            if (module.showCapes.get() && module.walls.get()) {
                color = ImColorW.toInt(module.layerOneColor.get());

                instance.renderToBuffer(poseStack, MC.renderBuffers().bufferSource().getBuffer(CoreRendering.entityTranslucent(state.skin.capeTexture(), true)), i, j, color);
            }
        }

    }
}
