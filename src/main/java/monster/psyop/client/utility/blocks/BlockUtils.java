package monster.psyop.client.utility.blocks;

import monster.psyop.client.config.Config;
import monster.psyop.client.config.sub.enums.AirPlaceMode;
import monster.psyop.client.utility.EntityUtils;
import monster.psyop.client.utility.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static monster.psyop.client.Liberty.MC;

public class BlockUtils {
    public static final Comparator<int[]> CLOSEST_XZ_COMPARATOR =
            Comparator.comparingDouble(
                    value -> {
                        if (value.length == 2) {
                            return MC.player != null
                                    ? MathUtils.xzDistanceBetween(
                                    MC.player.getX(),
                                    MC.player.getZ(),
                                    value[0],
                                    value[1])
                                    : 0;
                        } else if (value.length == 3) {
                            return MC.player != null
                                    ? MathUtils.xzDistanceBetween(
                                    MC.player.getX(),
                                    MC.player.getZ(),
                                    value[0],
                                    value[2])
                                    : 0;
                        }

                        return 0;
                    });
    public static final Direction[] HORIZONTALS = {
            Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST
    };
    public static final Direction[] ALL = {
            Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.UP, Direction.DOWN
    };
    public static Block[] SIGNS;
    private static final double MAGIC_PLACE_OFFSET = 0.0154;

    public static Direction[] getDirections() {
        return Direction.values();
    }

    public static String getKey(Block v) {
        return BuiltInRegistries.BLOCK.getKey(v).getPath();
    }

    public static double getHeight(BlockPos pos) {
        return MC.player.clientLevel.getBlockState(pos).getShape(MC.player.clientLevel, pos).max(Direction.Axis.Y);
    }

    public static boolean canPlace(BlockPos pos) {
        return canPlace(pos, 3.75, false);
    }

    public static boolean canPlace(BlockPos pos, double dist) {
        return canPlace(pos, dist, false);
    }

    public static boolean canPlace(BlockPos pos, double dist, boolean liquidPlace) {
        assert MC.player != null;

        List<Entity> entities =
                MC.player
                        .level()
                        .getEntities(null, new AABB(pos.getCenter().add(3, 3, 3), pos.getCenter().add(-3, -3, -3))).stream().filter(
                                entity -> {
                                    if (EntityUtils.canPlaceIn(entity)) {
                                        return false;
                                    }

                                    return entity.isColliding(pos, Blocks.BEDROCK.defaultBlockState());
                                }).toList();

        return isReplaceable(pos) && entities.isEmpty() && MC.player.getEyePosition().closerThan(getSafeHitResult(pos).getLocation(), dist) && (liquidPlace ? shouldLiquidPlace(pos) : !shouldAirPlace(pos));
    }

    public static boolean shouldAirPlace(BlockPos pos) {
        for (Direction direction : getDirections()) {
            if (!BlockUtils.isReplaceable(pos.relative(direction))) return false;
        }
        return true;
    }

    public static boolean shouldLiquidPlace(BlockPos pos) {
        return BlockUtils.isLiquid(pos.relative(Direction.DOWN));
    }

    public static boolean hasEntitiesInside(BlockPos pos) {
        assert MC.player != null;
        List<Entity> entities =
                MC.player
                        .level()
                        .getEntities(null, new AABB(pos.getCenter().add(3, 3, 3), pos.getCenter().add(-3, -3, -3))).stream().filter(
                                entity -> {
                                    if (EntityUtils.canPlaceIn(entity)) {
                                        return false;
                                    }

                                    return entity.isColliding(pos, Blocks.BEDROCK.defaultBlockState());
                                }).toList();
        return !entities.isEmpty();
    }

    public static BlockHitResult getSafeHitResult(BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        Direction direction = Direction.UP;

        Vec3 offset;
        double yHeight = 0;

        for (Direction dir : getDirections()) {
            // Performance!
            mutable.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
            if (!isReplaceable(mutable)) {
                yHeight = getHeight(mutable);

                direction = dir;

                if (dir == Direction.DOWN) {
                    break;
                }
            }
        }

        offset = clickOffset(pos, direction);

        if (yHeight <= 0.2) {
            offset = new Vec3(offset.x, Math.floor(offset.y) + MAGIC_PLACE_OFFSET, offset.z);
        }

        return new BlockHitResult(offset, direction.getOpposite(), mutable.set(pos.getX() + direction.getStepX(), pos.getY() + direction.getStepY(), pos.getZ() + direction.getStepZ()), false);
    }

    public static Vec3 clickOffset(BlockPos pos) {
        return clickOffset(pos, getPlaceDirection(pos));
    }

    public static Vec3 clickOffset(BlockPos pos, Direction direction) {
        return Vec3.atCenterOf(pos).add(direction.getStepX() * 0.5, direction.getStepY() * 0.5, direction.getStepZ() * 0.5);
    }

    public static boolean isItem(BlockPos pos, Item... items) {
        return MC.player != null
                && Arrays.stream(items)
                .toList()
                .contains(MC.player.level().getBlockState(pos).getBlock().asItem());
    }

    public static boolean isItem(BlockPos pos, List<Item> items) {
        return MC.player != null
                && items.contains(MC.player.level().getBlockState(pos).getBlock().asItem());
    }

    public static boolean isNotAir(BlockPos pos) {
        return MC.player == null || !MC.player.level().getBlockState(pos).isAir();
    }

    // Misc
    public static boolean isContainer(BlockPos pos) {
        assert MC.player != null;
        Block block = MC.player.level().getBlockState(pos).getBlock();
        return MC.player != null
                && (block == Blocks.CHEST
                || block == Blocks.TRAPPED_CHEST
                || isShulker(MC.player.level().getBlockState(pos).getBlock())
                || block == Blocks.BARREL
                || block == Blocks.ENDER_CHEST
                || block == Blocks.HOPPER);
    }

    public static boolean isContainer(Block block) {
        return MC.player != null
                && (block == Blocks.CHEST
                || block == Blocks.TRAPPED_CHEST
                || isShulker(block)
                || block == Blocks.BARREL
                || block == Blocks.ENDER_CHEST
                || block == Blocks.HOPPER);
    }

    public static boolean isExposedToAir(BlockPos pos) {
        for (Direction direction : ALL) {
            if (BlockUtils.isAir(pos.relative(direction))) return true;
        }
        return false;
    }

    public static boolean isShulker(Block block) {
        return getKey(block).endsWith("shulker_box");
    }

    public static boolean isBed(BlockPos pos) {
        assert MC.player != null;
        return getKey(MC.player.level().getBlockState(pos).getBlock()).endsWith("_bed");
    }

    public static boolean isBed(Block block) {
        return getKey(block).endsWith("_bed");
    }

    public static boolean testBlock(BlockPos pos, Predicate<Block> predicate) {
        return predicate.test(MC.player.level().getBlockState(pos).getBlock());
    }

    public static boolean isBlock(BlockPos pos, Block... blocks) {
        return MC.player != null
                && Arrays.stream(blocks).toList().contains(MC.player.level().getBlockState(pos).getBlock());
    }

    public static boolean isBlock(double x, double y, double z, Block... blocks) {
        return MC.player != null
                && Arrays.stream(blocks)
                .toList()
                .contains(
                        MC.player
                                .level()
                                .getBlockState(new BlockPos((int) x, (int) y, (int) z))
                                .getBlock());
    }

    public static boolean isBlock(int x, int y, int z, Block... blocks) {
        return MC.player != null
                && Arrays.stream(blocks)
                .toList()
                .contains(MC.player.level().getBlockState(new BlockPos(x, y, z)).getBlock());
    }

    public static boolean canInstantMine(BlockPos pos) {
        assert MC.player != null;
        return MC.player.level().getBlockState(pos).getDestroySpeed(MC.player.level(), pos) < 0.3;
    }

    public static boolean doesNotExplode(BlockPos pos) {
        if (isReplaceable(pos)) return true;
        assert MC.player != null;
        return MC.player.level().getBlockState(pos).getBlock().getExplosionResistance() > 100;
    }

    public static boolean isReplaceable(BlockPos pos) {
        return MC.player != null && MC.player.level().getBlockState(pos).isAir()
                || MC.player.level().getBlockState(pos).canBeReplaced();
    }

    public static boolean isLiquid(BlockPos pos) {
        return MC.player != null && !MC.player.level().getBlockState(pos).getFluidState().isEmpty();
    }

    public static BlockState getState(BlockPos pos) {
        assert MC.player != null;
        return MC.player.level().getBlockState(pos);
    }

    public static boolean canUse(BlockPos pos) {
        Block block = getState(pos).getBlock();
        return isContainer(pos)
                || block instanceof LeverBlock
                || block instanceof ButtonBlock
                || block instanceof DoorBlock
                || block instanceof DragonEggBlock
                || block instanceof AnvilBlock
                || block instanceof TrapDoorBlock
                || isBed(block);
    }

    // Crystal utilities
    public static boolean isAir(BlockPos pos) {
        return MC.player != null && MC.player.level().getBlockState(pos).isAir();
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        return isBlock(pos, Blocks.OBSIDIAN, Blocks.BEDROCK) && isAir(pos.relative(Direction.UP));
    }

    public static BlockPos getCevPos(Player player) {
        BlockPos pos = player.getOnPos().relative(Direction.UP);
        return null;
    }

    public static Direction getPlaceDirection(BlockPos pos) {
        if (Config.get().placing.airPlace == AirPlaceMode.Horizon) return Direction.UP;
        for (Direction direction : ALL) {
            if (!isAir(pos.relative(direction))) return direction;
        }
        return Direction.DOWN;
    }

    public static BlockPos isOnEntity(BlockPos pos, Entity entity) {
        for (Direction dir : ALL) {
            if (pos.relative(dir).equals(entity.blockPosition())) return pos.relative(dir);
            if (pos.relative(dir)
                    .relative(Direction.UP)
                    .equals(entity.blockPosition().relative(Direction.UP))) return pos.relative(dir);
        }
        return pos;
    }

    public static List<int[]> findNearBlocksByRadius(BlockPos.MutableBlockPos pos, int radius, Predicate<int[]> predicate) {
        if (MC.level == null) {
            return new ArrayList<>();
        }

        ArrayList<int[]> blockPosList = new ArrayList<>((radius * radius) * 2);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

        for (int bX = -radius; bX <= radius; bX++) {
            blockPos.setX(pos.getX() + bX);
            for (int bZ = -radius; bZ <= radius; bZ++) {
                blockPos.setZ(pos.getZ() + bZ);

                var posVec = new int[]{blockPos.getX(), blockPos.getY(), blockPos.getZ()};
                if (predicate.test(posVec)) {
                    blockPosList.add(posVec);
                }
            }
        }

        return blockPosList;
    }
}
