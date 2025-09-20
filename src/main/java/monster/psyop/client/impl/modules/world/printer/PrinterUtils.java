package monster.psyop.client.impl.modules.world.printer;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static monster.psyop.client.Psyop.MC;


public class PrinterUtils {

    public static Printer PRINTER;

    public static boolean shouldSwimUp() {
        return MC.player != null && MC.player.isInLiquid();
    }

    public static Optional<BlockPos> findTopBlock(BlockPos.MutableBlockPos pos) {
        if (MC.player == null) return Optional.empty();

        int liquidLevel = pos.getY();
        while (true) {
            pos.setY(liquidLevel++);
            if (!BlockUtils.isLiquid(pos)) {
                return Optional.of(pos.immutable());
            }
        }
    }

    public static Optional<BlockPos> findAirOpening(BlockPos pos, int radius) {
        List<int[]> blockPosList = findAirOpeningList(pos, radius);

        if (!blockPosList.isEmpty()) {
            blockPosList.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);
            return findTopBlock(
                    new BlockPos.MutableBlockPos(blockPosList.get(0)[0], pos.getY(), blockPosList.get(0)[1]));
        }

        return Optional.empty();
    }

    public static Optional<BlockPos> findClosestSuitableLand(BlockPos pos, int radius) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        // These two are split for a reason ^~^
        for (Direction dir : BlockUtils.HORIZONTALS) {
            blockPos.set(pos.getX() + dir.getStepX(), pos.getY() + 1, pos.getZ() + dir.getStepZ());

            if (BlockUtils.isNotAir(blockPos) && BlockUtils.getHeight(blockPos) <= 0.5 && !BlockUtils.isReplaceable(blockPos)) {
                return Optional.of(blockPos.immutable());
            }
        }

        for (Direction dir : BlockUtils.HORIZONTALS) {
            blockPos.set(pos.getX() + dir.getStepX(), pos.getY(), pos.getZ() + dir.getStepZ());

            if (BlockUtils.isNotAir(blockPos) && BlockUtils.getHeight(blockPos) <= 0.5 && !BlockUtils.isReplaceable(blockPos) && !BlockUtils.isNotAir(blockPos.offset(0, 1, 0))) {
                return Optional.of(blockPos.immutable());
            }
        }

        List<int[]> blockPosList = findSuitableLandList(pos, radius);

        if (!blockPosList.isEmpty()) {
            blockPosList.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);
            return Optional.of(new BlockPos(blockPosList.get(0)[0], pos.getY(), blockPosList.get(0)[1]));
        }

        return Optional.empty();
    }

    public static List<int[]> findSuitableLandList(BlockPos pos, int radius) {
        assert MC.level != null;
        List<int[]> blockPosList = new ArrayList<>((radius * radius) * 2);
        Optional<BlockPos> topBlock = findTopBlock(pos.mutable());

        if (topBlock.isEmpty()) {
            return blockPosList;
        }

        BlockPos.MutableBlockPos blockPos = topBlock.get().mutable();
        for (int x = -radius; x <= radius; x++) {
            blockPos.setX(pos.getX() + x);
            for (int z = -radius; z <= radius; z++) {
                blockPos.setZ(pos.getZ() + z);

                if (!BlockUtils.isReplaceable(blockPos)) {
                    BlockState bs = MC.level.getBlockState(blockPos);

                    if (BlockUtils.getHeight(blockPos.offset(0, 1, 0)) <= 0.5) {
                        blockPosList.add(new int[]{blockPos.getX(), blockPos.getZ()});
                    }
                }
            }
        }

        return blockPosList;
    }

    public static List<int[]> findSuitableLandListDontCareDidntAskPlusRatio(BlockPos pos, int radius) {
        assert MC.level != null;
        List<int[]> blockPosList = new ArrayList<>((radius * radius) * 2);
        Optional<BlockPos> topBlock = findTopBlock(pos.mutable());

        if (topBlock.isEmpty()) {
            return blockPosList;
        }

        BlockPos.MutableBlockPos blockPos = topBlock.get().mutable();
        for (int x = -radius; x <= radius; x++) {
            blockPos.setX(pos.getX() + x);
            for (int z = -radius; z <= radius; z++) {
                blockPos.setZ(pos.getZ() + z);

                if (!BlockUtils.isReplaceable(blockPos)) {
                    blockPosList.add(new int[]{blockPos.getX(), blockPos.getZ()});
                }
            }
        }

        return blockPosList;
    }

    public static List<int[]> findAirOpeningList(BlockPos pos, int radius) {
        List<int[]> blockPosList = new ArrayList<>();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int x = -radius; x <= radius; x++) {
            blockPos.setX(pos.getX() + x);
            for (int z = -radius; z <= radius; z++) {
                blockPos.setZ(pos.getZ() + z);

                Optional<BlockPos> topBlock = findTopBlock(blockPos);

                if (topBlock.isPresent() && !BlockUtils.isNotAir(topBlock.get()) && (isTouchingClimbableBlock(topBlock.get().relative(Direction.UP)) || isTouchingBlock(topBlock.get()))) {
                    blockPosList.add(new int[]{blockPos.getX(), blockPos.getZ()});
                }
            }
        }

        return blockPosList;
    }

    public static boolean isTouchingClimbableBlock(BlockPos pos) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (Direction dir : BlockUtils.HORIZONTALS) {
            blockPos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());

            if (BlockUtils.isNotAir(blockPos) && BlockUtils.getHeight(blockPos) <= 0.5 && !BlockUtils.isReplaceable(blockPos)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isTouchingBlock(BlockPos pos) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (Direction dir : BlockUtils.HORIZONTALS) {
            blockPos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());

            if (BlockUtils.isNotAir(blockPos) && BlockUtils.getHeight(blockPos) <= 0.5 && !BlockUtils.isReplaceable(blockPos) && !BlockUtils.isNotAir(blockPos.offset(0, 1, 0))) {
                return true;
            }
        }

        return false;
    }

    public static List<int[]> getPlaceableBlocksFromChunk(BlockPos pos, Predicate<int[]> predicate) {
        if (MC.level == null) {
            return new ArrayList<>();
        }

        ArrayList<int[]> blockPosList = new ArrayList<>(128);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) {
            return new ArrayList<>();
        }

        ChunkAccess chunk = MC.level.getChunk(blockPos);

        //if (worldSchematic.isChunkLoaded(chunk.getPos().x, chunk.getPos().z)) {
        for (int bX = 0; bX < 16; bX++) {
            blockPos.setX(chunk.getPos().getMinBlockX() + bX);
            for (int bZ = 0; bZ < 16; bZ++) {
                blockPos.setZ(chunk.getPos().getMaxBlockZ() + bZ);

                var posVec = new int[]{blockPos.getX(), blockPos.getY(), blockPos.getZ()};
                if (predicate.test(posVec)) {
                    blockPosList.add(posVec);
                }
            }
        }
        //}

        return blockPosList;
    }

    public static boolean placeBlock(
            InteractionHand hand, int itemResult, BlockPos pos) {
        assert MC.player != null;
        assert MC.gameMode != null;
        assert MC.getConnection() != null;
        assert MC.level != null;

        if (BlockUtils.isReplaceable(pos)) {
            MC.gameMode.useItemOn(MC.player, hand, BlockUtils.getSafeHitResult(pos));
            return true;
        }
        return false;
    }

    public static List<int[]> findNearBlocksByChunk(BlockPos pos, int chunkRadius, Predicate<int[]> predicate) {
        if (MC.level == null) {
            return new ArrayList<>();
        }

        ArrayList<int[]> blockPosList = new ArrayList<>(((chunkRadius * chunkRadius) * 128) * 2);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) {
            return new ArrayList<>();
        }

        int anchorBreaking = PRINTER.anchorBreakLimit.get();

        for (int[] chunkPos : PlacingManager.chunkScanningOrder) {
            blockPos.set(pos.getX() + (chunkPos[0] * 16), pos.getY(), pos.getZ() + (chunkPos[1] * 16));

            if (blockPos.getX() == 0 && blockPos.getZ() == 0) {
                blockPos.setX(pos.getX());
                blockPos.setZ(pos.getZ());
            }

            // Let's not needlessly scan for more yet.
            if (blockPosList.size() >= anchorBreaking) {
                break;
            }

            LevelChunk chunk = worldSchematic.getChunkAt(blockPos);

            if (!chunk.isEmpty()
                    && worldSchematic.isLoaded(blockPos)
                    && MC.level.isLoaded(blockPos)) {
                BlockPos.MutableBlockPos secBlockPos = new BlockPos.MutableBlockPos(0, pos.getY(), 0);

                for (int bX = 0; bX < 16; bX++) {
                    secBlockPos.setX(chunk.getPos().getMinBlockX() + bX);
                    for (int bZ = 0; bZ < 16; bZ++) {
                        secBlockPos.setZ(chunk.getPos().getMinBlockZ() + bZ);

                        var blockVec = new int[]{secBlockPos.getX(), secBlockPos.getY(), secBlockPos.getZ()};

                        if (predicate.test(blockVec)) {
                            blockPosList.add(blockVec);
                        }
                    }
                }
            }
        }

        return blockPosList;
    }
}
