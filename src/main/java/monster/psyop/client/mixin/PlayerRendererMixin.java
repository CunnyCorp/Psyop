package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.render.HandView;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType modifyHandBuffer(ResourceLocation resourceLocation) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.wizard.get()) {
                switch (module.bufferModifier.get()) {
                    case "quads":
                        return CoreRendering.quads();
                    case "lines":
                        return CoreRendering.lines();
                    case "item":
                        return CoreRendering.entityTranslucent(resourceLocation, true);
                    case "wireframe":
                        return CoreRendering.wireframe();
                }
            }
        }
        return RenderType.entityTranslucent(resourceLocation, true);
    }
}
