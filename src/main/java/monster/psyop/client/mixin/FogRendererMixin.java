package monster.psyop.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.render.WorldView;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @ModifyReturnValue(method = "computeFogColor", at = @At("RETURN"))
    public Vector4f computeFogColor(Vector4f original) {
        if (Liberty.MODULES.isActive(WorldView.class)) {
            WorldView module = Liberty.MODULES.get(WorldView.class);

            if (module.modifyFogColor.get()) {
                return new Vector4f(module.fogColor.value());
            }
        }

        return original;
    }
}
