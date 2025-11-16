package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.render.WorldView;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "getSkyLightLevel", at = @At(value = "HEAD"), cancellable = true)
    public void getSkyLightLevel(Entity entity, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        if (Psyop.MODULES.isActive(WorldView.class)) {
            WorldView module = Psyop.MODULES.get(WorldView.class);

            if (module.modifyEntityLighting.get()) {
                cir.setReturnValue(module.entityLight.get());
            }
        }
    }

    @Inject(method = "getBlockLightLevel", at = @At(value = "HEAD"), cancellable = true)
    public void getBlockLightLevel(Entity entity, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        if (Psyop.MODULES.isActive(WorldView.class)) {
            WorldView module = Psyop.MODULES.get(WorldView.class);

            if (module.modifyEntityLighting.get()) {
                cir.setReturnValue(module.entityLight.get());
            }
        }
    }
}
