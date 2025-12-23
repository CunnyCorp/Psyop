package monster.psyop.client.impl.modules.world.printer;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.ProvidedStringSetting;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import imgui.type.ImString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LitematicaPrinter extends Module {
    private final GroupedSettings sgGeneral = addGroup(new GroupedSettings("general", "Printer settings (state-aware)"));

    private static final int SENSITIVE_STABLE_TICKS = 3;
    private static final float SENSITIVE_ROT_EPS_DEG = 2.5f;
    private static final double SENSITIVE_MAX_H_SPEED = 0.05;

    private static final int FAIL_COOLDOWN_TICKS = 8;
    private static final int FAIL_MAX = 2;

    public final IntSetting switchDelay = new IntSetting.Builder()
            .name("switch-delay")
            .description("How long to wait (ticks) after switching items before placing.")
            .defaultTo(7)
            .range(0, 10)
            .addTo(sgGeneral);

    public final IntSetting dedicatedSlot = new IntSetting.Builder()
            .name("dedicated-slot")
            .description("Hotbar slot to use for placement (0-8).")
            .defaultTo(6)
            .range(0, 8)
            .addTo(sgGeneral);

    public final IntSetting placeRadius = new IntSetting.Builder()
            .name("place-radius")
            .description("XZ radius to scan for placeable blocks.")
            .defaultTo(5)
            .range(1, 6)
            .addTo(sgGeneral);

    public final FloatSetting placeDistance = new FloatSetting.Builder()
            .name("place-distance")
            .description("Max interaction distance used for placement.")
            .defaultTo(5.0f)
            .range(3.2f, 5.0f)
            .addTo(sgGeneral);

    public final IntSetting blocksPerTick = new IntSetting.Builder()
            .name("blocks/tick")
            .description("How many blocks to attempt per tick.")
            .defaultTo(4)
            .range(1, 8)
            .addTo(sgGeneral);

    public final IntSetting actionDelay = new IntSetting.Builder()
            .name("action-delay")
            .description("Delay (ticks) between placement attempts.")
            .defaultTo(0)
            .range(0, 20)
            .addTo(sgGeneral);

    public final IntSetting confirmTicks = new IntSetting.Builder()
            .name("confirm-ticks")
            .description("How long to wait for the world to reflect the placed block/state before failing (ticks).")
            .defaultTo(3)
            .range(0, 40)
            .addTo(sgGeneral);

    public final BoolSetting strictBlockMatch = new BoolSetting.Builder()
            .name("strict-block-match")
            .description("Only places the exact block item required by the schematic (no color swapping).")
            .defaultTo(true)
            .addTo(sgGeneral);

    public final BoolSetting respectBlockState = new BoolSetting.Builder()
            .name("respect-block-state")
            .description("Tries to place blocks with correct facing/axis/half/type.")
            .defaultTo(true)
            .addTo(sgGeneral);

    public final BoolSetting stopOnMismatch = new BoolSetting.Builder()
            .name("stop-on-mismatch")
            .description("Stops the module if a placed block does not match the required state.")
            .defaultTo(false)
            .addTo(sgGeneral);

    public final BoolSetting matchWaterlogged = new BoolSetting.Builder()
            .name("match-waterlogged")
            .description("Include WATERLOGGED in match checks (may be harder to satisfy).")
            .defaultTo(false)
            .addTo(sgGeneral);

    public final BoolSetting placeThroughWalls = new BoolSetting.Builder()
            .name("place-through-walls")
            .description("Allow placement without line-of-sight checks (more misplaces, but works behind walls).")
            .defaultTo(true)
            .addTo(sgGeneral);

    public final BoolSetting requireLineOfSight = new BoolSetting.Builder()
            .name("require-line-of-sight")
            .description("Only place when the chosen hit point is visible (raytrace hits the correct face).")
            .defaultTo(true)
            .visible((s) -> !placeThroughWalls.get())
            .addTo(sgGeneral);

    public final ProvidedStringSetting targetSort = new ProvidedStringSetting.Builder()
            .suggestions(List.of(new ImString("Nearest"), new ImString("BottomUp")))
            .name("target-sort")
            .description("How to choose the next target block.")
            .defaultTo(new ImString("BottomUp"))
            .addTo(sgGeneral);

    private int swapTimer = 0;
    private int actionTimer = 0;

    private BlockPos pendingPos = null;
    private BlockState pendingRequired = null;
    private int pendingTicks = 0;

    private float lastYaw = Float.NaN;
    private float lastPitch = Float.NaN;
    private int stableRotationTicks = 0;

    private final Map<BlockPos, Integer> failCooldowns = new HashMap<>();
    private final Map<BlockPos, Integer> failCounts = new HashMap<>();

    public LitematicaPrinter() {
        super(Categories.WORLD, "litematica-printer", "Prints Litematica schematics with state-aware placement (facing, half, axis).");
    }

    @Override
    public boolean controlsHotbar() {
        return true;
    }

    @Override
    public boolean inUse() {
        return swapTimer > 0 || pendingPos != null;
    }

    @Override
    protected void enabled() {
        super.enabled();
        swapTimer = 0;
        actionTimer = 0;
        pendingPos = null;
        pendingRequired = null;
        pendingTicks = 0;
        lastYaw = Float.NaN;
        lastPitch = Float.NaN;
        stableRotationTicks = 0;
        failCooldowns.clear();
        failCounts.clear();
    }

    @Override
    public void update() {
        if (MC.player == null || MC.level == null || MC.gameMode == null) return;

        WorldSchematic schematic = SchematicWorldHandler.getSchematicWorld();
        if (schematic == null) {
            active(false);
            return;
        }

        if (swapTimer > 0) swapTimer--;
        if (actionTimer > 0) actionTimer--;

        decayFailCooldowns();
        updateStability();

        if (pendingPos != null && pendingRequired != null) {
            BlockState actual = MC.level.getBlockState(pendingPos);
            if (matchesRequired(actual, pendingRequired)) {
                pendingPos = null;
                pendingRequired = null;
                pendingTicks = 0;
            } else {
                pendingTicks--;
                if (pendingTicks <= 0) {
                    if (stopOnMismatch.get()) {
                        NotificationManager.get().addNotification(
                                "LitematicaPrinter",
                                "Placement mismatch. Stopping to avoid misplace.",
                                NotificationEvent.Type.ERROR,
                                8000L
                        );
                        active(false);
                        return;
                    }
                    pendingPos = null;
                    pendingRequired = null;
                }
            }
            return;
        }

        if (actionTimer > 0 || swapTimer > 0) return;
        if (MC.player.isUsingItem()) return;

        int attempts = Math.max(1, blocksPerTick.get());
        for (int i = 0; i < attempts; i++) {
            Target t = findNextTarget(schematic);
            if (t == null) break;

            Item requiredItem = t.required.getBlock().asItem();
            int invSlot = InventoryUtils.findMatchingSlot((stack, slot) -> stack.getItem() == requiredItem);
            if (invSlot == -1) continue;

            if (!ensureHolding(invSlot, requiredItem)) return;

            Placement plan = respectBlockState.get()
                    ? planPlacement(t.pos, t.required, MC.player.getMainHandItem())
                    : Placement.fallback(t.pos);

            if (plan == null) {
                noteFailed(t.pos);
                continue;
            }

            MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, plan.hitResult);

            pendingPos = t.pos;
            pendingRequired = t.required;
            pendingTicks = Math.max(0, confirmTicks.get());
            actionTimer = Math.max(0, actionDelay.get());
            break;
        }
    }

    private void updateStability() {
        if (MC.player == null) return;

        float yaw = MC.player.getYRot();
        float pitch = MC.player.getXRot();

        if (Float.isNaN(lastYaw) || Float.isNaN(lastPitch)) {
            lastYaw = yaw;
            lastPitch = pitch;
            stableRotationTicks = 0;
            return;
        }

        float dy = Math.abs(Mth.wrapDegrees(yaw - lastYaw));
        float dp = Math.abs(Mth.wrapDegrees(pitch - lastPitch));

        if (dy <= SENSITIVE_ROT_EPS_DEG && dp <= SENSITIVE_ROT_EPS_DEG) stableRotationTicks++;
        else stableRotationTicks = 0;

        lastYaw = yaw;
        lastPitch = pitch;
    }

    private boolean canPlaceSensitiveNow() {
        if (MC.player == null) return false;

        if (!MC.player.onGround()) return false;
        if (MC.player.isSprinting()) return false;

        Vec3 v = MC.player.getDeltaMovement();
        double hSpeed = Math.sqrt(v.x * v.x + v.z * v.z);
        if (hSpeed > SENSITIVE_MAX_H_SPEED) return false;

        return stableRotationTicks >= SENSITIVE_STABLE_TICKS;
    }

    private static boolean isStateSensitive(BlockState required) {
        if (required == null) return false;
        return required.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                || required.hasProperty(BlockStateProperties.FACING)
                || required.hasProperty(BlockStateProperties.AXIS)
                || required.hasProperty(BlockStateProperties.HALF)
                || required.hasProperty(BlockStateProperties.SLAB_TYPE);
    }

    private boolean ensureHolding(int inventorySlot, Item requiredItem) {
        if (MC.player == null) return false;
        if (swapTimer > 0) return false;

        InventoryUtils.swapSlot(dedicatedSlot.get());
        if (MC.player.getMainHandItem().getItem() != requiredItem) {
            InventoryUtils.swapToHotbar(inventorySlot, dedicatedSlot.get());
            swapTimer = switchDelay.get();
            return false;
        }
        return true;
    }

    private Target findNextTarget(WorldSchematic schematic) {
        if (MC.player == null || MC.level == null) return null;

        int r = placeRadius.get();
        BlockPos.MutableBlockPos base = MC.player.blockPosition().mutable();
        List<Target> candidates = new ArrayList<>();
        List<Target> sensitiveCandidates = new ArrayList<>();

        for (int dy = -r; dy <= r; dy++) {
            base.setY(MC.player.getBlockY() + dy);
            List<int[]> positions = BlockUtils.findNearBlocksByRadius(base, r, (posVec) -> true);
            for (int[] v : positions) {
                BlockPos p = new BlockPos(v[0], v[1], v[2]);
                BlockState required = schematic.getBlockState(p);
                if (required.isAir()) continue;

                BlockState current = MC.level.getBlockState(p);
                if (matchesRequired(current, required)) continue;
                if (!current.canBeReplaced()) continue;
                if (MC.player.getBoundingBox().intersects(Vec3.atLowerCornerOf(p), Vec3.atLowerCornerOf(p).add(1, 1, 1))) continue;

                if (!BlockUtils.canPlace(p, Math.min(placeDistance.get(), (float) MC.player.blockInteractionRange()))) continue;

                Item requiredItem = required.getBlock().asItem();
                if (InventoryUtils.findAnySlot(requiredItem) == -1) continue;

                if (failCooldowns.getOrDefault(p, 0) > 0) continue;

                Target target = new Target(p, required);
                if (isStateSensitive(required) && canPlaceSensitiveTarget(target)) {
                    sensitiveCandidates.add(target);
                } else {
                    candidates.add(target);
                }
            }
        }

        // Priority: state-sensitive blocks that can be placed right now
        if (!sensitiveCandidates.isEmpty()) {
            String mode = targetSort.get();
            boolean bottomUp = mode != null && mode.equalsIgnoreCase("BottomUp");

            if (bottomUp) {
                sensitiveCandidates.sort((a, b) -> {
                    if (MC.player == null) return 0;
                    int ay = a.pos.getY();
                    int by = b.pos.getY();
                    if (ay != by) return Integer.compare(ay, by);
                    return Double.compare(
                        MC.player.distanceToSqr(a.pos.getX() + 0.5, a.pos.getY() + 0.5, a.pos.getZ() + 0.5),
                        MC.player.distanceToSqr(b.pos.getX() + 0.5, b.pos.getY() + 0.5, b.pos.getZ() + 0.5)
                    );
                });
            } else {
                sensitiveCandidates.sort(Comparator.comparingDouble(t -> {
                    if (MC.player == null) return 0;
                    return MC.player.distanceToSqr(t.pos.getX() + 0.5, t.pos.getY() + 0.5, t.pos.getZ() + 0.5);
                }));
            }
            return sensitiveCandidates.get(0);
        }

        // Fallback: regular blocks
        if (candidates.isEmpty()) return null;

        String mode = targetSort.get();
        boolean bottomUp = mode != null && mode.equalsIgnoreCase("BottomUp");

        if (bottomUp) {
            candidates.sort((a, b) -> {
                if (MC.player == null) return 0;

                int ay = a.pos.getY();
                int by = b.pos.getY();
                if (ay != by) return Integer.compare(ay, by);

                double da = MC.player.distanceToSqr(a.pos.getX() + 0.5, a.pos.getY() + 0.5, a.pos.getZ() + 0.5);
                double db = MC.player.distanceToSqr(b.pos.getX() + 0.5, b.pos.getY() + 0.5, b.pos.getZ() + 0.5);
                return Double.compare(da, db);
            });
        } else {
            candidates.sort(Comparator.comparingDouble(t -> {
                if (MC.player == null) return 0;
                return MC.player.distanceToSqr(t.pos.getX() + 0.5, t.pos.getY() + 0.5, t.pos.getZ() + 0.5);
            }));
        }
        return candidates.get(0);
    }

    private boolean canPlaceSensitiveTarget(Target target) {
        if (!isStateSensitive(target.required)) return true;
        if (!canPlaceSensitiveNow()) return false;

        // Quick check: try to find a valid placement
        Placement plan = planPlacement(target.pos, target.required, MC.player.getMainHandItem());
        return plan != null;
    }

    private void decayFailCooldowns() {
        if (failCooldowns.isEmpty()) return;
        failCooldowns.replaceAll((p, v) -> v <= 0 ? 0 : v - 1);
        failCooldowns.entrySet().removeIf(e -> e.getValue() <= 0);
    }

    private void noteFailed(BlockPos pos) {
        if (pos == null) return;
        int c = failCounts.getOrDefault(pos, 0) + 1;
        if (c >= FAIL_MAX) {
            failCounts.remove(pos);
            failCooldowns.put(pos, FAIL_COOLDOWN_TICKS);
        } else {
            failCounts.put(pos, c);
        }
    }

    private Placement planPlacement(BlockPos pos, BlockState required, ItemStack stack) {
        if (MC.player == null || MC.level == null) return null;

        Item item = stack.getItem();
        if (!(item instanceof BlockItem blockItem)) return null;
        Block block = blockItem.getBlock();

        for (Direction placeFace : BlockUtils.ALL) {
            BlockPos neighbor = pos.relative(placeFace.getOpposite());
            BlockState neighborState = MC.level.getBlockState(neighbor);

            if (neighborState.canBeReplaced()) continue;
            if (BlockUtils.canUse(neighbor)) continue;

            for (Vec3 hitLoc : sampleHitLocations(neighbor, placeFace, required)) {
                if (requireLineOfSight.get() && !placeThroughWalls.get()) {
                    if (!isVisibleHit(neighbor, placeFace, hitLoc)) continue;
                }

                BlockHitResult hit = new BlockHitResult(hitLoc, placeFace, neighbor, false);
                BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(MC.player, InteractionHand.MAIN_HAND, hit));
                BlockState predicted = block.getStateForPlacement(ctx);

                if (predicted != null && matchesRequired(predicted, required)) {
                    return new Placement(hit, predicted);
                }
            }
        }

        return null;
    }

    private boolean isVisibleHit(BlockPos neighbor, Direction face, Vec3 hitLoc) {
        if (MC.player == null || MC.level == null) return false;

        Vec3 eyes = MC.player.getEyePosition();
        return isVisibleHitWith(eyes, hitLoc, neighbor, face, ClipContext.Block.COLLIDER)
                || isVisibleHitWith(eyes, hitLoc, neighbor, face, ClipContext.Block.OUTLINE);
    }

    private boolean isVisibleHitWith(Vec3 eyes, Vec3 hitLoc, BlockPos neighbor, Direction face, ClipContext.Block blockMode) {
        if (MC.level == null || MC.player == null) return false;
        HitResult res = MC.level.clip(new ClipContext(
                eyes,
                hitLoc,
                blockMode,
                ClipContext.Fluid.NONE,
                MC.player
        ));
        if (res.getType() != HitResult.Type.BLOCK) return false;
        BlockHitResult bhr = (BlockHitResult) res;
        return bhr.getBlockPos().equals(neighbor) && bhr.getDirection() == face;
    }

    private List<Vec3> sampleHitLocations(BlockPos neighbor, Direction face, BlockState required) {
        if (MC.level == null) return List.of();

        VoxelShape shape = MC.level.getBlockState(neighbor).getCollisionShape(MC.level, neighbor);
        AABB bb;
        try {
            bb = shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();
        } catch (Throwable t) {
            bb = new AABB(0, 0, 0, 1, 1, 1);
        }

        double eps = 0.001;
        double minX = bb.minX;
        double minY = bb.minY;
        double minZ = bb.minZ;
        double maxX = bb.maxX;
        double maxY = bb.maxY;
        double maxZ = bb.maxZ;

        boolean wantTop = false;
        boolean wantBottom = false;

        if (required.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            var type = required.getValue(BlockStateProperties.SLAB_TYPE);
            wantTop = type.toString().equalsIgnoreCase("top");
            wantBottom = type.toString().equalsIgnoreCase("bottom");
        } else if (required.hasProperty(BlockStateProperties.HALF)) {
            String half = required.getValue(BlockStateProperties.HALF).toString().toLowerCase();
            wantTop = half.contains("top");
            wantBottom = half.contains("bottom");
        }

        double[] xs = {0.15, 0.35, 0.5, 0.65, 0.85};
        double[] zs = {0.15, 0.35, 0.5, 0.65, 0.85};
        double[] ysSide = wantTop ? new double[]{0.80, 0.95} : wantBottom ? new double[]{0.05, 0.20} : new double[]{0.25, 0.75};

        List<Vec3> out = new ArrayList<>(32);

        if (face == Direction.UP || face == Direction.DOWN) {
            double y = face == Direction.UP ? (neighbor.getY() + maxY - eps) : (neighbor.getY() + minY + eps);
            for (double xMul : xs) {
                double x = neighbor.getX() + lerp(minX, maxX, xMul);
                for (double zMul : zs) {
                    double z = neighbor.getZ() + lerp(minZ, maxZ, zMul);
                    out.add(new Vec3(x, y, z));
                }
            }
            return out;
        }

        double xFace = neighbor.getX() + 0.5;
        double zFace = neighbor.getZ() + 0.5;
        if (face == Direction.EAST) xFace = neighbor.getX() + maxX - eps;
        else if (face == Direction.WEST) xFace = neighbor.getX() + minX + eps;
        else if (face == Direction.SOUTH) zFace = neighbor.getZ() + maxZ - eps;
        else if (face == Direction.NORTH) zFace = neighbor.getZ() + minZ + eps;

        for (double yMul : ysSide) {
            double y = neighbor.getY() + lerp(minY, maxY, yMul);
            if (face == Direction.EAST || face == Direction.WEST) {
                for (double zMul : zs) {
                    double z = neighbor.getZ() + lerp(minZ, maxZ, zMul);
                    out.add(new Vec3(xFace, y, z));
                }
            } else {
                for (double xMul : xs) {
                    double x = neighbor.getX() + lerp(minX, maxX, xMul);
                    out.add(new Vec3(x, y, zFace));
                }
            }
        }

        return out;
    }

    private static double lerp(double min, double max, double t) {
        return min + (max - min) * t;
    }

    private boolean matchesRequired(BlockState actual, BlockState required) {
        if (actual == null || required == null) return false;
        if (actual.isAir() && required.isAir()) return true;
        if (actual.getBlock() != required.getBlock()) return false;
        if (required.hasProperty(BlockStateProperties.AXIS) && actual.hasProperty(BlockStateProperties.AXIS)) {
            if (actual.getValue(BlockStateProperties.AXIS) != required.getValue(BlockStateProperties.AXIS)) return false;
        }

        if (required.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && actual.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            if (actual.getValue(BlockStateProperties.HORIZONTAL_FACING) != required.getValue(BlockStateProperties.HORIZONTAL_FACING)) return false;
        }

        if (required.hasProperty(BlockStateProperties.FACING) && actual.hasProperty(BlockStateProperties.FACING)) {
            if (actual.getValue(BlockStateProperties.FACING) != required.getValue(BlockStateProperties.FACING)) return false;
        }

        if (required.hasProperty(BlockStateProperties.SLAB_TYPE) && actual.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            if (actual.getValue(BlockStateProperties.SLAB_TYPE) != required.getValue(BlockStateProperties.SLAB_TYPE)) return false;
        }

        if (required.hasProperty(BlockStateProperties.HALF) && actual.hasProperty(BlockStateProperties.HALF)) {
            if (!actual.getValue(BlockStateProperties.HALF).equals(required.getValue(BlockStateProperties.HALF))) return false;
        }

        if (matchWaterlogged.get() && required.hasProperty(BlockStateProperties.WATERLOGGED) && actual.hasProperty(BlockStateProperties.WATERLOGGED)) {
            if (actual.getValue(BlockStateProperties.WATERLOGGED) != required.getValue(BlockStateProperties.WATERLOGGED)) return false;
        }

        return true;
    }

    private static boolean hasHalf(BlockState state) {
        return state.hasProperty(BlockStateProperties.HALF);
    }

    private static Object getHalfValue(BlockState state) {
        if (state.hasProperty(BlockStateProperties.HALF)) return state.getValue(BlockStateProperties.HALF);
        return null;
    }

    private record Target(BlockPos pos, BlockState required) {
    }

    private record Placement(BlockHitResult hitResult, BlockState predicted) {
        static Placement fallback(BlockPos pos) {
            return new Placement(BlockUtils.getSafeHitResult(pos), null);
        }
    }

    private record Candidate(BlockHitResult hit, BlockState predicted) {
    }
}


