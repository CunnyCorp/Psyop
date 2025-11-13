package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.render.ItemView;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class ItemStackRenderState$LayerRenderStateMixin {
    @Shadow
    public abstract void setRenderType(RenderType renderType);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderItem(Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II[ILjava/util/List;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"))
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(ItemView.class)) {
            ItemView module = Psyop.MODULES.get(ItemView.class);

            switch (module.bufferModifier.get()) {
                case "quads":
                    setRenderType(CoreRendering.quads());
                    return;
                case "lines":
                    setRenderType(CoreRendering.lines());
                    return;
                case "item":
                    setRenderType(CoreRendering.glintTranslucent());
            }
        }
    }
}
