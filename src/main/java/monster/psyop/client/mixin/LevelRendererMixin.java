package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.render.BlockLights;
import monster.psyop.client.impl.modules.render.Chams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LevelRenderer.class, priority = 749)
public class LevelRendererMixin {
    @Unique
    private EntityType<?> lastGlowingEntityType;

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V"))
    public void setColor0(OutlineBufferSource instance, int i, int j, int k, int l) {
        if (Liberty.MODULES.isActive(Chams.class)) {
            Chams module = Liberty.MODULES.get(Chams.class);
            if (module.glowEntities.value().contains(lastGlowingEntityType)) {
                float[] glowColor = module.glowEntities.colorMap.getOrDefault(lastGlowingEntityType, module.entityGlowColor.get());
                instance.setColor((int) (glowColor[0] * 255), (int) (glowColor[1] * 255), (int) (glowColor[2] * 255), (int) (glowColor[3] * 255));
                return;
            }
        }

        instance.setColor(i, j, k, l);
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
    public boolean shouldEntityAppearGlowing0(Minecraft instance, Entity entity) {
        lastGlowingEntityType = entity.getType();
        if (Liberty.MODULES.isActive(Chams.class)) {
            Chams module = Liberty.MODULES.get(Chams.class);
            if (module.glowEntities.value().contains(entity.getType())) {
                return true;
            }
        }

        return instance.shouldEntityAppearGlowing(entity);
    }

    // BlockLights - 0
    @Redirect(method = "getLightColor(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private static int getLightColor0(BlockState instance) {
        if (Liberty.MODULES.isActive(BlockLights.class)) {
            BlockLights module = Liberty.MODULES.get(BlockLights.class);
            if (module.blockList.value().contains(instance.getBlock())) {
                return module.light.get();
            }
        }

        return instance.getLightEmission();
    }

    // BlockLights - 1
    @Redirect(method = "getLightColor(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;emissiveRendering(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean getLightColor0(BlockState instance, BlockGetter blockGetter, BlockPos blockPos) {
        if (Liberty.MODULES.isActive(BlockLights.class)) {
            BlockLights module = Liberty.MODULES.get(BlockLights.class);
            if (module.blockList.value().contains(instance.getBlock())) {
                return true;
            }
        }

        return instance.emissiveRendering(blockGetter, blockPos);
    }
}
