package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.render.ItemView;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static monster.psyop.client.Psyop.MC;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Redirect(method = "renderQuadList", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFII)V"))
    private static void renderQuadList(VertexConsumer instance, PoseStack.Pose pose, BakedQuad bakedQuad, float f, float g, float h, float i, int j, int k) {
        if (Psyop.MODULES.isActive(ItemView.class)) {
            ItemView module = Psyop.MODULES.get(ItemView.class);

            RenderType renderType = switch (module.bufferModifier.get().get()) {
                case "quads" -> CoreRendering.seeThroughQuads();
                case "lines" -> CoreRendering.seeThroughLines();
                case "item" -> CoreRendering.glintTranslucent();
                default -> CoreRendering.wireframe();
            };

            if (module.modifyItemColor.get()) {
                f = module.itemColor.get()[0];
                g = module.itemColor.get()[1];
                h = module.itemColor.get()[2];
                i = module.itemColor.get()[3];
            }

            if (module.bufferModifier.get().get().equals("none")) {
                instance.putBulkData(pose, bakedQuad, f, g, h, i, j, k);
            } else {
                MC.renderBuffers().bufferSource().getBuffer(renderType).putBulkData(pose, bakedQuad, f, g, h, i, j, k);
            }
            return;
        }

        instance.putBulkData(pose, bakedQuad, f, g, h, i, j, k);
    }

    // ItemView - ItemColor
    @Inject(method = "getLayerColorSafe", at = @At("HEAD"), cancellable = true)
    private static void modifyLayerColorsForSodium(int[] is, int i, CallbackInfoReturnable<Integer> cir) {
        if (Psyop.MODULES.isActive(ItemView.class)) {
            ItemView module = Psyop.MODULES.get(ItemView.class);

            if (module.modifyItemColor.get()) {
                cir.setReturnValue(ARGB.colorFromFloat(module.itemColor.get()[3], module.itemColor.get()[0], module.itemColor.get()[1], module.itemColor.get()[2]));
            }
        }
    }
}
