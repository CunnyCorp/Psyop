package monster.psyop.client.impl.modules.world;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutoPortal extends Module {
    private enum Stage {
        PLACING_FRAME,
        IGNITING,
        DONE
    }

    private final GroupedSettings sgGeneral = addGroup(new GroupedSettings("general", "Build + place settings"));

    private final IntSetting switchDelay = new IntSetting.Builder()
            .name("switch-delay")
            .description("How long to wait (ticks) after switching items before placing/using.")
            .defaultTo(4)
            .range(0, 10)
            .addTo(sgGeneral);

    private final IntSetting dedicatedSlot = new IntSetting.Builder()
            .name("dedicated-slot")
            .description("Hotbar slot to use for obsidian/flint-and-steel (0-8).")
            .defaultTo(7)
            .range(0, 8)
            .addTo(sgGeneral);

    private final IntSetting blocksPerTick = new IntSetting.Builder()
            .name("blocks/tick")
            .description("How many obsidian blocks to attempt per tick.")
            .defaultTo(3)
            .range(1, 4)
            .addTo(sgGeneral);

    private final IntSetting actionDelay = new IntSetting.Builder()
            .name("action-delay")
            .description("Delay between placement/ignite actions (ticks). Helps servers register actions reliably.")
            .defaultTo(2)
            .range(0, 20)
            .addTo(sgGeneral);

    private final BoolSetting onlyOnGround = new BoolSetting.Builder()
            .name("on-ground")
            .description("Only builds when you are on the ground.")
            .defaultTo(true)
            .addTo(sgGeneral);

    private final BoolSetting notInLiquid = new BoolSetting.Builder()
            .name("out-of-liquid")
            .description("Only builds when you are not in liquid.")
            .defaultTo(true)
            .addTo(sgGeneral);

    private final FloatSetting placeDistance = new FloatSetting.Builder()
            .name("place-distance")
            .description("Max distance to the hit position for placing/using.")
            .defaultTo(5.0f)
            .range(3.2f, 5.0f)
            .addTo(sgGeneral);

    private final IntSetting forwardOffset = new IntSetting.Builder()
            .name("forward-offset")
            .description("How far in front of you to build the portal (blocks).")
            .defaultTo(3)
            .range(1, 6)
            .addTo(sgGeneral);

    private final IntSetting rightOffset = new IntSetting.Builder()
            .name("right-offset")
            .description("Shift the portal left/right relative to your facing direction (blocks).")
            .defaultTo(-1)
            .range(-6, 6)
            .addTo(sgGeneral);

    private final IntSetting searchRadius = new IntSetting.Builder()
            .name("search-radius")
            .description("Search radius around the initial position to find a valid place for the portal (blocks).")
            .defaultTo(5)
            .range(0, 8)
            .addTo(sgGeneral);

    private final IntSetting stuckTimeout = new IntSetting.Builder()
            .name("stuck-timeout")
            .description("How long to wait (ticks) without progress before giving up.")
            .defaultTo(600)
            .range(10, 600)
            .addTo(sgGeneral);

    private final IntSetting attemptCooldown = new IntSetting.Builder()
            .name("attempt-cooldown")
            .description("Cooldown (ticks) before retrying the same frame block again.")
            .defaultTo(6)
            .range(0, 20)
            .addTo(sgGeneral);

    private final IntSetting minPlayerDistance = new IntSetting.Builder()
            .name("min-player-distance")
            .description("Minimum XZ distance (blocks) between you and the portal base to avoid building inside you.")
            .defaultTo(2)
            .range(0, 6)
            .addTo(sgGeneral);

    private final IntSetting igniteDelay = new IntSetting.Builder()
            .name("ignite-delay")
            .description("Delay (ticks) after finishing the frame before igniting.")
            .defaultTo(8)
            .range(0, 60)
            .addTo(sgGeneral);

    private final IntSetting igniteCooldown = new IntSetting.Builder()
            .name("ignite-cooldown")
            .description("Cooldown (ticks) between ignition attempts.")
            .defaultTo(6)
            .range(0, 60)
            .addTo(sgGeneral);

    private final BoolSetting autoDisable = new BoolSetting.Builder()
            .name("auto-disable")
            .description("Disable the module after building/igniting.")
            .defaultTo(true)
            .addTo(sgGeneral);

    private final List<BlockPos> remainingFrame = new ArrayList<>();
    private BlockPos ignitePos;
    private BlockPos base;
    private Direction facing;
    private Direction right;
    private Stage stage = Stage.PLACING_FRAME;
    private int swapTimer = 0;
    private int igniteAttempts = 0;
    private int stuckTicks = 0;
    private BlockPos lastAttemptPos;
    private int lastAttemptTimer = 0;
    private int actionTimer = 0;
    private int waitBeforeIgnite = 0;
    private int igniteTimer = 0;
    private int lastRemainingCount = -1;

    public AutoPortal() {
        super(Categories.WORLD, "auto-portal", "Builds a simple obsidian nether portal frame in front of you and ignites it.");
    }

    @Override
    public boolean controlsHotbar() {
        return true;
    }

    @Override
    public void enabled() {
        super.enabled();
        resetPlan();
        if (!buildPlan()) {
            active(false);
        }
    }

    @Override
    protected void disabled() {
        super.disabled();
        resetPlan();
    }

    private void resetPlan() {
        remainingFrame.clear();
        ignitePos = null;
        base = null;
        facing = null;
        right = null;
        stage = Stage.PLACING_FRAME;
        swapTimer = 0;
        igniteAttempts = 0;
        stuckTicks = 0;
        lastAttemptPos = null;
        lastAttemptTimer = 0;
        actionTimer = 0;
        waitBeforeIgnite = 0;
        igniteTimer = 0;
        lastRemainingCount = -1;
    }

    private boolean passesChecks() {
        if (MC.player == null || MC.level == null || MC.gameMode == null) return false;
        if (onlyOnGround.get() && !MC.player.onGround()) return false;
        if (MC.player.isUsingItem()) return false;
        return !notInLiquid.get() || !MC.player.isInLiquid();
    }

    private static Direction safeHorizontal(Direction d) {
        if (d == Direction.UP || d == Direction.DOWN) return Direction.NORTH;
        return d;
    }

    private boolean buildPlan() {
        if (MC.player == null || MC.level == null) return false;

        facing = safeHorizontal(MC.player.getDirection());
        right = facing.getClockWise();

        BlockPos initial = MC.player.blockPosition()
                .relative(facing, forwardOffset.get())
                .relative(right, rightOffset.get());

        base = findBestBase(initial);

        if (base == null) {
            NotificationManager.get().addNotification(
                    "AutoPortal",
                    "No suitable place found nearby. Move to a flatter spot or increase search-radius.",
                    NotificationEvent.Type.ERROR,
                    9000L
            );
            return false;
        }

        remainingFrame.clear();
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                boolean isFrame = (w == 0 || w == 3 || h == 0 || h == 4);
                if (!isFrame) continue;
                remainingFrame.add(base.relative(right, w).above(h).immutable());
            }
        }

        ignitePos = base.relative(right, 1).above(1).immutable();

        return true;
    }

    private BlockPos findBestBase(BlockPos initial) {
        if (MC.player == null || MC.level == null || right == null) return null;

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        int r = Math.max(0, searchRadius.get());
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                BlockPos cand = initial.offset(dx, 0, dz);
                if (!isValidBase(cand)) continue;

                double d = MC.player.distanceToSqr(cand.getX() + 0.5, cand.getY() + 0.5, cand.getZ() + 0.5);
                if (d < bestDist) {
                    bestDist = d;
                    best = cand.immutable();
                }
            }
        }

        return best;
    }

    private boolean isValidBase(BlockPos candBase) {
        if (MC.level == null || right == null || MC.player == null) return false;

        int minDist = Math.max(0, minPlayerDistance.get());
        if (minDist > 0) {
            if (Math.abs(candBase.getX() - MC.player.getBlockX()) < minDist
                    && Math.abs(candBase.getZ() - MC.player.getBlockZ()) < minDist) {
            }
        }
        for (int w = 1; w <= 2; w++) {
            for (int h = 1; h <= 3; h++) {
                BlockPos p = candBase.relative(right, w).above(h);
                if (!MC.level.getBlockState(p).canBeReplaced() && !MC.level.getBlockState(p).is(Blocks.NETHER_PORTAL)) {
                    return false;
                }
            }
        }

        AABB playerBB = MC.player.getBoundingBox().inflate(0.05);
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                BlockPos p = candBase.relative(right, w).above(h);
                if (playerBB.intersects(new AABB(p))) return false;
            }
        }
        for (int w = 1; w <= 2; w++) {
            for (int h = 1; h <= 3; h++) {
                BlockPos p = candBase.relative(right, w).above(h);
                if (playerBB.intersects(new AABB(p))) return false;
            }
        }

        int dx = candBase.getX() - MC.player.getBlockX();
        int dz = candBase.getZ() - MC.player.getBlockZ();
        if (minDist > 0 && (Math.abs(dx) < minDist && Math.abs(dz) < minDist)) return false;

        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                boolean isFrame = (w == 0 || w == 3 || h == 0 || h == 4);
                if (!isFrame) continue;
                BlockPos p = candBase.relative(right, w).above(h);
                if (MC.level.getBlockState(p).is(Blocks.OBSIDIAN)) continue;
                if (!MC.level.getBlockState(p).canBeReplaced()) return false;
            }
        }

        boolean hasStart = false;
        for (int w = 0; w < 4; w++) {
            BlockPos p = candBase.relative(right, w);
            if (MC.level.getBlockState(p).is(Blocks.OBSIDIAN) || BlockUtils.canPlace(p, placeDistance.get())) {
                hasStart = true;
                break;
            }
        }
        return hasStart;
    }

    private boolean ensureHolding(Item item) {
        if (MC.player == null) return false;

        if (swapTimer > 0) {
            swapTimer--;
            return false;
        }

        int slot = InventoryUtils.findAnySlot(item);
        if (slot == -1) return false;

        InventoryUtils.swapSlot(dedicatedSlot.get());

        if (MC.player.getMainHandItem().getItem() != item) {
            InventoryUtils.swapToHotbar(slot, dedicatedSlot.get());
            swapTimer = switchDelay.get();
            return false;
        }

        return true;
    }

    private void cleanupPlacedFrameBlocks() {
        if (MC.level == null) return;
        for (Iterator<BlockPos> it = remainingFrame.iterator(); it.hasNext(); ) {
            BlockPos p = it.next();
            if (MC.level.getBlockState(p).is(Blocks.OBSIDIAN)) {
                it.remove();
            }
        }
    }

    private BlockPos findNextPlaceableFrameBlock() {
        if (MC.level == null || MC.player == null) return null;

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos p : remainingFrame) {
            if (!MC.level.getBlockState(p).canBeReplaced() && !MC.level.getBlockState(p).is(Blocks.OBSIDIAN)) {
                NotificationManager.get().addNotification(
                        "AutoPortal",
                        "Blocked while building (a solid block is in the frame path).",
                        NotificationEvent.Type.ERROR,
                        8000L
                );
                stage = Stage.DONE;
                return null;
            }

            if (lastAttemptTimer > 0 && lastAttemptPos != null && lastAttemptPos.equals(p)) {
                continue;
            }

            if (!BlockUtils.canPlace(p, placeDistance.get())) continue;

            double d = MC.player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }

        return best;
    }

    private boolean igniteOnce() {
        if (MC.player == null || MC.level == null || MC.gameMode == null || ignitePos == null) return false;

        BlockPos clickBlock = ignitePos.below();
        if (!MC.level.getBlockState(clickBlock).is(Blocks.OBSIDIAN)) {
            BlockPos found = null;
            for (Direction d : BlockUtils.ALL) {
                if (d == Direction.UP) continue;
                BlockPos nb = ignitePos.relative(d);
                if (MC.level.getBlockState(nb).is(Blocks.OBSIDIAN)) {
                    found = nb;
                    break;
                }
            }
            if (found == null) return false;
            clickBlock = found;
        }

        BlockHitResult hit = new BlockHitResult(
                BlockUtils.clickOffset(clickBlock, Direction.UP),
                Direction.UP,
                clickBlock,
                false
        );

        MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, hit);
        return true;
    }

    @EventListener
    public void onTick(monster.psyop.client.impl.events.game.OnTick.Pre e) {
        if (!passesChecks()) return;

        if (actionTimer > 0) actionTimer--;

        if (stage == Stage.DONE) {
            if (autoDisable.get()) active(false);
            return;
        }

        if (remainingFrame.isEmpty() && stage == Stage.PLACING_FRAME) {
            stage = Stage.IGNITING;
            waitBeforeIgnite = Math.max(0, igniteDelay.get());
            igniteTimer = Math.max(0, igniteCooldown.get());
        }

        if (remainingFrame.isEmpty() && ignitePos == null) {
            resetPlan();
            if (!buildPlan()) {
                active(false);
            }
            return;
        }

        if (stage == Stage.PLACING_FRAME) {
            cleanupPlacedFrameBlocks();
            if (lastAttemptTimer > 0) lastAttemptTimer--;

            int toPlace = Math.max(1, blocksPerTick.get());
            int before = remainingFrame.size();

            for (int i = 0; i < toPlace; i++) {
                if (actionTimer > 0) break;

                if (InventoryUtils.findAnySlot(Blocks.OBSIDIAN.asItem()) == -1) {
                    NotificationManager.get().addNotification(
                            "AutoPortal",
                            "No obsidian found in inventory.",
                            NotificationEvent.Type.ERROR,
                            7000L
                    );
                    stage = Stage.DONE;
                    return;
                }

                if (!ensureHolding(Blocks.OBSIDIAN.asItem())) {
                    return;
                }

                BlockPos target = findNextPlaceableFrameBlock();
                if (target == null) break;

                MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, BlockUtils.getSafeHitResult(target));
                lastAttemptPos = target.immutable();
                lastAttemptTimer = attemptCooldown.get();
                actionTimer = Math.max(0, actionDelay.get());
            }

            if (remainingFrame.isEmpty()) {
                stage = Stage.IGNITING;
                stuckTicks = 0;
                waitBeforeIgnite = Math.max(0, igniteDelay.get());
                igniteTimer = Math.max(0, igniteCooldown.get());
                return;
            }

            int after = remainingFrame.size();
            boolean progressed = after < before;

            if (!progressed) {
                stuckTicks++;
                if (stuckTicks >= stuckTimeout.get()) {
                    NotificationManager.get().addNotification(
                            "AutoPortal",
                            "Stuck: can't place any remaining frame blocks. Move closer / clear space / increase place-distance.",
                            NotificationEvent.Type.WARNING,
                            9000L
                    );
                    stage = Stage.DONE;
                }
            } else {
                stuckTicks = 0;
            }
            return;
        }

        if (stage == Stage.IGNITING) {
            if (waitBeforeIgnite > 0) {
                waitBeforeIgnite--;
                return;
            }

            if (igniteTimer > 0) {
                igniteTimer--;
                return;
            }

            if (actionTimer > 0) return;

            if (MC.level.getBlockState(ignitePos).is(Blocks.NETHER_PORTAL)) {
                NotificationManager.get().addNotification(
                        "AutoPortal",
                        "Portal is active.",
                        NotificationEvent.Type.SUCCESS,
                        5000L
                );
                stage = Stage.DONE;
                return;
            }

            if (!MC.level.getBlockState(ignitePos).canBeReplaced() && !MC.level.getBlockState(ignitePos).is(Blocks.NETHER_PORTAL)) {
                NotificationManager.get().addNotification(
                        "AutoPortal",
                        "Cannot ignite: inside of the frame is blocked.",
                        NotificationEvent.Type.ERROR,
                        7000L
                );
                stage = Stage.DONE;
                return;
            }

            if (InventoryUtils.findAnySlot(Items.FLINT_AND_STEEL) == -1) {
                NotificationManager.get().addNotification(
                        "AutoPortal",
                        "No flint and steel found in inventory.",
                        NotificationEvent.Type.ERROR,
                        7000L
                );
                stage = Stage.DONE;
                return;
            }

            if (!ensureHolding(Items.FLINT_AND_STEEL)) {
                return;
            }

            igniteOnce();
            igniteAttempts++;
            igniteTimer = Math.max(0, igniteCooldown.get());
            actionTimer = Math.max(0, actionDelay.get());

            if (igniteAttempts >= 10) {
                if (MC.level.getBlockState(ignitePos).is(Blocks.NETHER_PORTAL)) {
                    NotificationManager.get().addNotification(
                            "AutoPortal",
                            "Portal is active.",
                            NotificationEvent.Type.SUCCESS,
                            5000L
                    );
                } else {
                    NotificationManager.get().addNotification(
                            "AutoPortal",
                            "Tried igniting, but portal didn't activate. Check the frame & space inside.",
                            NotificationEvent.Type.WARNING,
                            8000L
                    );
                }
                stage = Stage.DONE;
            }
        }
    }
}
