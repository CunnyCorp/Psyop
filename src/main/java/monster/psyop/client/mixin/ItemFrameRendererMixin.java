package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.render.InvisibleFrames;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemFrameRenderer.class, priority = 999)
public abstract class ItemFrameRendererMixin {
    @Redirect(method = "render(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;isInvisible:Z"))
    public boolean renderMap(ItemFrameRenderState value) {
        if (Psyop.MODULES.isActive(InvisibleFrames.class)) {
            if (value.mapId != null || !value.item.isEmpty()) {
                return true;
            }
        }

        return value.isInvisible;
    }
}
