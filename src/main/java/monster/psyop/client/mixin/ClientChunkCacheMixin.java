package monster.psyop.client.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {
    @Redirect(
            method = "replaceBiomes",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;inRange(II)Z"))
    public boolean a(ClientChunkCache.Storage instance, int i, int j) {
        return true;
    }

    @Redirect(
            method = "replaceWithPacketData",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;inRange(II)Z"))
    public boolean b(ClientChunkCache.Storage instance, int i, int j) {
        return true;
    }
}
