package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.render.ItemView;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
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

    @Redirect(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static VertexConsumer getBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {

        if (Psyop.MODULES.isActive(ItemView.class)) {
            ItemView module = Psyop.MODULES.get(ItemView.class);

            if (module.walls.get()) {
                return VertexMultiConsumer.create(multiBufferSource.getBuffer(CoreRendering.glintTranslucent()), multiBufferSource.getBuffer(renderType));
            }
        }

        return ItemRenderer.getFoilBuffer(multiBufferSource, renderType, bl, bl2);
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
