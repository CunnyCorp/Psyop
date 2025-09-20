package monster.psyop.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.modules.render.HandView;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 777)
public abstract class GameRendererMixin {
    @Shadow
    protected abstract void bobHurt(PoseStack poseStack, float f);

    // HandView - NoHurt
    @Redirect(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    public void renderItemInHand0(GameRenderer instance, PoseStack poseStack, float f) {
        if (Psyop.MODULES.isActive(HandView.class)) {
            HandView module = Psyop.MODULES.get(HandView.class);

            if (module.noHurt.get()) {
                return;
            }
        }

        bobHurt(poseStack, f);
    }

    @Inject(method = "renderLevel", at = @At(value = "TAIL", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderScreenEffect(ZF)V"))
    public void renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        Psyop.EVENT_HANDLER.call(OnRender.get());

    }
}
