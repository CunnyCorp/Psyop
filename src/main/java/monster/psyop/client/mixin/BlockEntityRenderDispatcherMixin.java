package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.render.Chams;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlockEntityRenderDispatcher.class, priority = 749)
public abstract class BlockEntityRenderDispatcherMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z"))
    private <T extends BlockEntity> boolean shouldRender(BlockEntityRenderer<T> instance, T blockEntity, Vec3 vec3) {
        if (Psyop.MODULES.isActive(Chams.class)) {
            Chams module = Psyop.MODULES.get(Chams.class);

            if (module.blockEntities.value().contains(blockEntity.getType()) || module.alwaysRenderBlockEntities.get()) {
                return true;
            }
        }

        return instance.shouldRender(blockEntity, vec3);
    }
}
