package monster.psyop.client.utility.litematica;

import fi.dy.masa.litematica.world.ChunkSchematic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static monster.psyop.client.Liberty.MC;

@SuppressWarnings("unused")
public class LiteChunks {
    public List<ChunkSchematic> chunkMap = new ArrayList<>();
    public Map<ChunkSchematic, List<Item>> chunkItemMap = new HashMap<>();
    public Item currentTarget = Items.WHITE_CARPET;
    @Nullable
    protected ChunkSchematic currentChunk = null;

    public List<BlockPos> scanChunk(ChunkSchematic chunk) {
        if (chunk == null) return new ArrayList<>();

        List<BlockPos> blocks = new ArrayList<>();

        refreshItems(chunk);

        ChunkPos pos = chunk.getPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos blockPos = pos.getBlockAt(x, LiteHacks.currentProject.getOrigin().getY() + 1, z);
                BlockState state = chunk.getBlockState(blockPos);
                if (state == null || state.isAir()) continue;
                blocks.add(blockPos);

                addItem(chunk, state.getBlock().asItem());
            }
        }

        return blocks;
    }

    public ChunkSchematic currentChunk() {
        return currentChunk;
    }

    public void remove(ChunkSchematic chunk) {
        chunkMap.remove(chunk);
    }

    public void findNextChunk(Predicate<ChunkSchematic> filter) {
        chunkMap.sort(
                Comparator.comparingInt(
                        (c) ->
                                c.getPos()
                                        .getChessboardDistance(
                                                MC.player != null ? MC.player.chunkPosition() : ChunkPos.ZERO)));
        for (ChunkSchematic chunk : chunkMap) {
            if (isEmpty(chunk)) continue;

            if (!filter.test(chunk)) continue;

            currentChunk = chunk;
            return;
        }

        currentChunk = null;
    }

    public void addChunk(ChunkSchematic chunk) {
        if (!chunkItemMap.containsKey(chunk)) chunkMap.add(chunk);
        chunkItemMap.putIfAbsent(chunk, new ArrayList<>());
        if (scanChunk(chunk).isEmpty()) {
            chunkMap.remove(chunk);
        }
    }

    public boolean isEmpty(ChunkSchematic chunk) {
        return !chunkItemMap.containsKey(chunk) || chunkItemMap.get(chunk).isEmpty();
    }

    private void refreshItems(ChunkSchematic chunk) {
        chunkItemMap.get(chunk).clear();
    }

    private void addItem(ChunkSchematic chunk, Item item) {
        if (!chunkItemMap.get(chunk).contains(item)) chunkItemMap.get(chunk).add(item);
    }
}
