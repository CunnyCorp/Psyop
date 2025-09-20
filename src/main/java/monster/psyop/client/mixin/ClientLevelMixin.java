package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.movement.PlayerTimer;
import monster.psyop.client.impl.modules.movement.Warp;
import monster.psyop.client.impl.modules.render.Chams;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

import static monster.psyop.client.Psyop.MC;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "getGloballyRenderedBlockEntities", at = @At("RETURN"), cancellable = true)
    public void modifyGlobalBlockEntities(CallbackInfoReturnable<Set<BlockEntity>> cir) {
        if (Psyop.MODULES.isActive(Chams.class)) {
            Chams module = Psyop.MODULES.get(Chams.class);

            Set<BlockEntity> blockEntities = new HashSet<>();

            for (int x = -(MC.options.renderDistance().get() + 2); x <= MC.options.renderDistance().get() + 2; x++) {
                for (int z = -(MC.options.renderDistance().get() + 2); z <= MC.options.renderDistance().get() + 2; z++) {
                    if (MC.level.hasChunk(MC.player.chunkPosition().x + x, MC.player.chunkPosition().z + z)) {
                        ChunkAccess chunk = MC.level.getChunk(MC.player.chunkPosition().x + x, MC.player.chunkPosition().z + z);

                        for (BlockPos entPos : chunk.getBlockEntitiesPos()) {
                            BlockEntity blockEntity = chunk.getBlockEntity(entPos);

                            if (blockEntity == null) {
                                continue;
                            }

                            if (module.alwaysRenderBlockEntities.get() || module.glowBlockEntities.value().contains(blockEntity.getType())) {
                                blockEntities.add(blockEntity);
                            }
                        }
                    }
                }
            }

            if (!blockEntities.isEmpty()) {
                cir.setReturnValue(blockEntities);
            }
        }
    }

    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    public void tickNonPassenger(Entity entity, CallbackInfo ci) {
        assert MC.player != null;

        if (MC.player == entity) {
            if (Psyop.MODULES.isActive(Warp.class)) {
                Warp module = Psyop.MODULES.get(Warp.class);

                if (!MC.options.keyJump.isDown() || MC.player.onGround()) {
                    return;
                }

                for (int i = 0; i <= module.multiplier.get(); i++) {
                    if (!module.onlyAir.get() || !MC.player.onGround()) MC.player.tick();
                }
            }

            if (Psyop.MODULES.isActive(PlayerTimer.class)) {
                PlayerTimer module = Psyop.MODULES.get(PlayerTimer.class);

                for (int i = 0; i <= module.multiplier.get(); i++) {
                    MC.player.tick();
                }

                if (module.lastBurst == 0) {
                    for (int i = 0; i < module.burstMultiplier.get(); i++) {
                        MC.player.tick();
                    }

                    module.lastBurst = module.burstDelay.get();
                }
            }
        }
    }
}
