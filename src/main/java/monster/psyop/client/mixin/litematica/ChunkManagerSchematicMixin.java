package monster.psyop.client.mixin.litematica;

import fi.dy.masa.litematica.world.ChunkManagerSchematic;
import fi.dy.masa.litematica.world.ChunkSchematic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkManagerSchematic.class, remap = false)
public class ChunkManagerSchematicMixin {
    @Shadow
    @Final
    private Long2ObjectMap<ChunkSchematic> loadedChunks;

    @Inject(method = "loadChunk", at = @At("TAIL"))
    public void loadChunk(int chunkX, int chunkZ, CallbackInfo ci) {
        //LiteHacks.liteChunks.addChunk(loadedChunks.get(ChunkPos.asLong(chunkX, chunkZ)));
    }
}
