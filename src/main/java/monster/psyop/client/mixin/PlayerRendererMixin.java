package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.render.HandView;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    public VertexConsumer modifyHandBuffer(MultiBufferSource instance, RenderType renderType) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.wizard.get()) {
                switch (module.bufferModifier.get().get()) {
                    case "quads":
                        return instance.getBuffer(CoreRendering.quads());
                    case "lines":
                        return instance.getBuffer(CoreRendering.lines());
                    case "item":
                        return instance.getBuffer(CoreRendering.glintTranslucent());
                    case "wireframe":
                        return instance.getBuffer(CoreRendering.wireframe());
                }
            }
        }
        return instance.getBuffer(renderType);
    }
}
