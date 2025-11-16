package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.rendering.CoreRendering;
import monster.psyop.client.impl.modules.render.AntiBlinker;
import monster.psyop.client.impl.modules.render.HandView;
import monster.psyop.client.impl.modules.render.NoRender;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
