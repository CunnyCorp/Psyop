package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.world.FastBreak;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Redirect(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    public float getDestroyProgress(BlockState instance, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        if (Psyop.MODULES.isActive(FastBreak.class)) {
            FastBreak module = Psyop.MODULES.get(FastBreak.class);

            return (instance.getDestroyProgress(player, blockGetter, blockPos) * module.breakMultiplier.get());
        }
        return instance.getDestroyProgress(player, blockGetter, blockPos);
    }
}
