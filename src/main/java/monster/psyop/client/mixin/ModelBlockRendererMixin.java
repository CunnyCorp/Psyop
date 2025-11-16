package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.render.ItemView;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @Redirect(method = "renderQuadList", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFII)V"))
    private static void renderQuadList(VertexConsumer instance, PoseStack.Pose pose, BakedQuad bakedQuad, float f, float g, float h, float i, int j, int k) {
        if (Psyop.MODULES.isActive(ItemView.class)) {
            ItemView module = Psyop.MODULES.get(ItemView.class);

            if (module.modifyItemColor.get()) {
                f = module.itemColor.get()[0];
                g = module.itemColor.get()[1];
                h = module.itemColor.get()[2];
                i = module.itemColor.get()[3];
            }

            instance.putBulkData(pose, bakedQuad, f, g, h, i, j, k);
            return;
        }

        instance.putBulkData(pose, bakedQuad, f, g, h, i, j, k);
    }

    @Redirect(method = "putQuadData", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[IIZ)V"))
    private static void renderQuadList(VertexConsumer instance, PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, float i, int[] is, int j, boolean bl) {
        if (Psyop.MODULES.isActive(ItemView.class)) {
            ItemView module = Psyop.MODULES.get(ItemView.class);

            if (module.modifyItemColor.get()) {
                f = module.itemColor.get()[0];
                g = module.itemColor.get()[1];
                h = module.itemColor.get()[2];
                i = module.itemColor.get()[3];
            }

            instance.putBulkData(pose, bakedQuad, fs, f, g, h, i, is, j, bl);
            return;
        }

        instance.putBulkData(pose, bakedQuad, fs, f, g, h, i, is, j, bl);
    }
}
