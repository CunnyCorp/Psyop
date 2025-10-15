package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.PsyopRenderTypes;
import monster.psyop.client.impl.modules.render.Chams;
import monster.psyop.client.impl.modules.render.ItemView;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static monster.psyop.client.Psyop.MC;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    // ItemView - ItemColor
    @Redirect(method = "renderQuadList", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFII)V"))
    private static void modifyItemColor(VertexConsumer instance, PoseStack.Pose pose, BakedQuad bakedQuad, float f, float g, float h, float i, int j, int k) {
        if (Psyop.MODULES.isActive(ItemView.class)) {
            ItemView module = Psyop.MODULES.get(ItemView.class);

            if (module.modifyItemColor.get()) {
                instance.putBulkData(pose, bakedQuad, module.itemColor.get()[0], module.itemColor.get()[1], module.itemColor.get()[2], module.itemColor.get()[3], j, k);
                return;
            }
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
