package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.render.WorldView;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LightTexture.class, priority = 749)
public class LightTextureMixin {
    @Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;forceBrightLightmap()Z"))
    private boolean forceBrightLightmap(DimensionSpecialEffects instance) {
        if (Liberty.MODULES.isActive(WorldView.class)) {
            WorldView module = Liberty.MODULES.get(WorldView.class);

            if (module.forceLightMap.get()) {
                return true;
            }
        }
        return instance.forceBrightLightmap();
    }

    @Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;ambientLight()F"))
    private float ambientLight(DimensionType instance) {
        if (Liberty.MODULES.isActive(WorldView.class)) {
            WorldView module = Liberty.MODULES.get(WorldView.class);

            if (module.modifyAmbient.get()) {
                return module.ambientLight.get();
            }
        }
        return instance.ambientLight();
    }
}
