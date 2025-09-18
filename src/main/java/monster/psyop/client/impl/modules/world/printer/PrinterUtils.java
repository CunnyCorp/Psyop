package monster.psyop.client.impl.modules.world.printer;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.orbit.EventHandler;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PrinterUtils {

    public static Printer PRINTER;
    public static FakePlayerEntity fakePlayer;

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(PrinterUtils.class);
    }

    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;

        initFakePlayer();

        PacketUtils.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    public static void updateFakePlayer(float pitch, float yaw) {
        PrinterUtils.fakePlayer.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        PrinterUtils.fakePlayer.getAttributes().assignAllValues(mc.player.getAttributes());
        PrinterUtils.fakePlayer.setPose(mc.player.getPose());
        PrinterUtils.fakePlayer.setXRot(pitch);
        PrinterUtils.fakePlayer.xRotO = pitch;
        PrinterUtils.fakePlayer.setYRot(yaw);
        PrinterUtils.fakePlayer.yRotO = yaw;
        PrinterUtils.fakePlayer.yBodyRot = yaw;
        PrinterUtils.fakePlayer.yBodyRotO = yaw;
        PrinterUtils.fakePlayer.yHeadRot = yaw;
        PrinterUtils.fakePlayer.yHeadRotO = yaw;
        PrinterUtils.fakePlayer.setYHeadRot(yaw);
        PrinterUtils.fakePlayer.setYBodyRot(yaw);
        PrinterUtils.fakePlayer.calculateEntityAnimation(true);
    }

    public static boolean shouldPauseActions() {
        return Modules.get().get(AutoEat.class).eating
                || Modules.get().get(AutoGap.class).isEating()
                || Modules.get().get(KillAura.class).getTarget() != null;
    }

    public static boolean isNight() {
        return mc.level != null && Math.floor(((double) mc.level.getTimeOfDay(0f) / 12000L) % 2) == 1;
    }

    public static int getTimeSinceLastRest() {
        if (mc.player == null) return -1;

        return Mth.clamp(
                mc.player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)),
                1,
                Integer.MAX_VALUE);
    }

    public static boolean shouldSwimUp() {
        return mc.player != null && mc.player.isInLiquid();
    }

    public static Optional<BlockPos> findTopBlock(BlockPos.MutableBlockPos pos) {
        if (mc.player == null) return Optional.empty();

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
        assert mc.level != null;
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
                    BlockState bs = mc.level.getBlockState(blockPos);

                    if (BlockUtils.getHeight(blockPos.offset(0, 1, 0)) <= 0.5) {
                        blockPosList.add(new int[]{blockPos.getX(), blockPos.getZ()});
                    }
                }
            }
        }

        return blockPosList;
    }

    public static List<int[]> findSuitableLandListDontCareDidntAskPlusRatio(BlockPos pos, int radius) {
        assert mc.level != null;
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

    public static void initFakePlayer() {
        if (mc.player != null) {
            if (fakePlayer == null || mc.player.clientLevel != fakePlayer.clientLevel) {
                fakePlayer = new FakePlayerEntity(mc.player, "~", 1000, false);
            }
        }
    }

    public static List<int[]> getPlaceableBlocksFromChunk(BlockPos pos, Predicate<int[]> predicate) {
        if (mc.level == null) {
            return new ArrayList<>();
        }

        ArrayList<int[]> blockPosList = new ArrayList<>(128);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) {
            return new ArrayList<>();
        }

        ChunkAccess chunk = mc.level.getChunk(blockPos);

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
        assert mc.player != null;
        assert mc.gameMode != null;
        assert mc.getConnection() != null;
        assert mc.level != null;

        if (BlockUtils.isReplaceable(pos)) {
            mc.gameMode.useItemOn(mc.player, hand, BlockUtils.getSafeHitResult(pos));
            return true;
        }
        return false;
    }

    public static List<int[]> findNearBlocksByChunk(BlockPos pos, int chunkRadius, Predicate<int[]> predicate) {
        if (mc.level == null) {
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
                    && mc.level.isLoaded(blockPos)) {
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
