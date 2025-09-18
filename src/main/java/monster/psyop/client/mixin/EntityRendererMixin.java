package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.render.WorldView;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow
    @Final
    private Font font;

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    public abstract Font getFont();


    /*@Redirect(method = "renderNameTag", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;isDiscrete:Z"))
    public boolean alwaysNotDiscrete(EntityRenderState instance) {
        return false;
    }

    @Redirect(method = "renderNameTag", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Font$DisplayMode;NORMAL:Lnet/minecraft/client/gui/Font$DisplayMode;"))
    public Font.DisplayMode modifyDisplayMode() {
        return Font.DisplayMode.SEE_THROUGH;
    }

    @Redirect(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getBackgroundOpacity(F)F"))
    public float modifyOpacity(Options instance, float f) {
        return 1.0f;
    }*/

    @Inject(method = "getSkyLightLevel", at = @At(value = "HEAD"), cancellable = true)
    public void getSkyLightLevel(Entity entity, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        if (Liberty.MODULES.isActive(WorldView.class)) {
            WorldView module = Liberty.MODULES.get(WorldView.class);

            if (module.modifyEntityLighting.get()) {
                cir.setReturnValue(module.entityLight.get());
            }
        }
    }

    @Inject(method = "getBlockLightLevel", at = @At(value = "HEAD"), cancellable = true)
    public void getBlockLightLevel(Entity entity, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        if (Liberty.MODULES.isActive(WorldView.class)) {
            WorldView module = Liberty.MODULES.get(WorldView.class);

            if (module.modifyEntityLighting.get()) {
                cir.setReturnValue(module.entityLight.get());
            }
        }
    }
}
