package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.render.BlockLights;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlockLightEngine.class, priority = 749)
public class BlockLightEngineMixin {
    @Redirect(method = "getEmission", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int forceBrightLightmap(BlockState instance) {
        if (Liberty.MODULES.isActive(BlockLights.class)) {
            BlockLights module = Liberty.MODULES.get(BlockLights.class);

            if (module.blockList.value().contains(instance.getBlock())) {
                return module.light.get();
            }
        }

        return instance.getLightEmission();
    }
}
