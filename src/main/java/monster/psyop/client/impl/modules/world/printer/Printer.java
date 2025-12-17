package monster.psyop.client.impl.modules.world.printer;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.framework.modules.settings.wrappers.ImBlockPos;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.modules.world.printer.movesets.AdvancedMove;
import monster.psyop.client.impl.modules.world.printer.movesets.BaritoneMove;
import monster.psyop.client.impl.modules.world.printer.movesets.VanillaMove;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.MathUtils;
import monster.psyop.client.utility.McDataCache;
import monster.psyop.client.utility.blocks.BlockUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Printer extends Module {
    // anchoringTo must be here.
    public static BlockPos.MutableBlockPos anchoringTo = new BlockPos.MutableBlockPos();
    public static BlockPos.MutableBlockPos lastPlacedBlock = new BlockPos.MutableBlockPos();
    public final List<int[]> toSort = new ArrayList<>();
    public final List<MapColor> containedColors = new ArrayList<>();
    public final List<Item> containedBlocks = new ArrayList<>();
    // Render
    final List<int[]> anchorToSort = new ArrayList<>();

    // Fade-out effect for recently placed blocks
    private static final class FadeEntry {
        final BlockPos pos;
        final long startMs;

        FadeEntry(BlockPos pos, long startMs) {
            this.pos = pos;
            this.startMs = startMs;
        }
    }

    private final List<FadeEntry> fading = new ArrayList<>();

    // Rendering settings
    private final GroupedSettings renderGroup = addGroup(new GroupedSettings("render", "Visual overlay for Printer"));
    public final BoolSetting renderEnabled = new BoolSetting.Builder()
            .name("enabled")
            .description("Enable Printer rendering overlays")
            .defaultTo(true)
            .addTo(renderGroup);
    public final BoolSetting renderAnchor = new BoolSetting.Builder()
            .name("show-anchor")
            .description("Draw anchor target (line and box)")
            .defaultTo(true)
            .addTo(renderGroup);
    public final BoolSetting renderQueue = new BoolSetting.Builder()
            .name("show-queue")
            .description("Draw boxes for queued blocks to place")
            .defaultTo(true)
            .addTo(renderGroup);
    public final IntSetting renderQueueLimit = new IntSetting.Builder()
            .name("queue-limit")
            .description("Max queued blocks to render for performance")
            .defaultTo(200)
            .range(10, 2000)
            .addTo(renderGroup);
    public final FloatSetting renderLineWidth = new FloatSetting.Builder()
            .name("line-width")
            .description("Line width for tracers")
            .defaultTo(1.5f)
            .range(0.5f, 6.0f)
            .addTo(renderGroup);
    public final ColorSetting anchorColor = new ColorSetting.Builder()
            .name("anchor-color")
            .defaultTo(new float[]{1.0f, 0.8f, 0.1f, 1.0f})
            .addTo(renderGroup);
    public final ColorSetting queueColor = new ColorSetting.Builder()
            .name("queue-color")
            .defaultTo(new float[]{0.2f, 0.6f, 1.0f, 0.8f})
            .addTo(renderGroup);
    public final IntSetting fadeDurationMs = new IntSetting.Builder()
            .name("fade-ms")
            .description("How long placed-block highlight lingers and shrinks (ms)")
            .defaultTo(650)
            .range(100, 4000)
            .addTo(renderGroup);

    // Settings;
    public final IntSetting swapDelay =
            new IntSetting.Builder()
                    .name("switch-delay")
                    .description("How long to wait before placing after switching.")
                    .defaultTo(4)
                    .range(0, 10)
                    .addTo(coreGroup);
    public final IntSetting dedicatedSlot =
            new IntSetting.Builder()
                    .name("dedicated-slot")
                    .description("The hotbar slot to use for blocks.")
                    .defaultTo(7)
                    .range(0, 8)
                    .addTo(coreGroup);
    public final IntSetting placeRadius =
            new IntSetting.Builder()
                    .name("place-radius")
                    .description("The range to check for placeable blocks.")
                    .defaultTo(5)
                    .range(1, 5)
                    .addTo(coreGroup);
    public final IntSetting delay =
            new IntSetting.Builder()
                    .name("printing-delay")
                    .description("Delay between printing blocks in ticks.")
                    .defaultTo(0)
                    .range(0, 20)
                    .addTo(coreGroup);
    public final IntSetting blocksPerTick =
            new IntSetting.Builder()
                    .name("blocks/tick")
                    .description("How many blocks place per tick.")
                    .defaultTo(3)
                    .range(1, 4)
                    .addTo(coreGroup);
    public final BoolSetting liquidPlace =
            new BoolSetting.Builder()
                    .name("liquid-place")
                    .description("Places inside of liquids if it would let you place a target block.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final IntSetting liquidPlaceTimeout =
            new IntSetting.Builder()
                    .name("liquid-place-timeout")
                    .description("Timeout between liquid placing, 0 to effectively disable.")
                    .defaultTo(10)
                    .range(0, 40)
                    .addTo(coreGroup);
    public final BoolSetting onlyOnGround =
            new BoolSetting.Builder()
                    .name("on-ground")
                    .description("Only places if the player is on-ground.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final BoolSetting notInLiquid =
            new BoolSetting.Builder()
                    .name("out-of-liquid")
                    .description("Only places if the player is not in a liquid.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final BoolSetting yLock = new BoolSetting.Builder()
            .name("y-lock")
            .defaultTo(true)
            .addTo(coreGroup);
    public final IntSetting yLockLevel = new IntSetting.Builder()
            .name("y-level")
            .defaultTo(63)
            .range(-64, 320)
            .addTo(coreGroup);
    public final BoolSetting yControl = new BoolSetting.Builder()
            .name("y-control")
            .description("Control Y level with + and -")
            .defaultTo(true)
            .addTo(coreGroup);

    // Color Swapping
    private final GroupedSettings colorSwapping = addGroup(new GroupedSettings("Color Swapping", "Color related settings!"));
    public final BoolSetting strictNoColor =
            new BoolSetting.Builder()
                    .name("strict-no-color")
                    .description("Prevents using color swapping at all.")
                    .defaultTo(false)
                    .addTo(colorSwapping);
    public final BlockListSetting blockExclusion =
            new BlockListSetting.Builder()
                    .name("block-exclusion")
                    .description("Excludes blocks.")
                    .defaultTo(new ArrayList<>())
                    .addTo(colorSwapping);
    // Anchoring
    private final GroupedSettings anchoring = addGroup(new GroupedSettings("Anchoring", "anchoring: for when you can't stop hugging blocks! -Lamb"));
    public final BoolSetting anchor =
            new BoolSetting.Builder()
                    .name("anchor")
                    .description("Anchors player to placeable blocks.")
                    .defaultTo(false)
                    .addTo(anchoring);
    public final IntSetting yLevel =
            new IntSetting.Builder()
                    .name("y-level")
                    .description("The Y level to scan")
                    .defaultTo(63)
                    .range(-64, 320)
                    .addTo(anchoring);
    public final BoolSetting useBaritone =
            new BoolSetting.Builder()
                    .name("use-baritone")
                    .description("Uses baritone for movement.")
                    .defaultTo(false)
                    .addTo(anchoring);
    public final BoolSetting alwaysSprint =
            new BoolSetting.Builder()
                    .name("always-sprint")
                    .description("Advanced move will always sprint.")
                    .visible((s) -> !useBaritone.get())
                    .defaultTo(false)
                    .addTo(anchoring);
    public final BoolSetting safeWalk =
            new BoolSetting.Builder()
                    .name("safe-walk")
                    .description("Attempts to walk safely.")
                    .visible((s) -> !useBaritone.get())
                    .defaultTo(true)
                    .addTo(anchoring);
    public final IntSetting backHoldTime =
            new IntSetting.Builder()
                    .name("back-hold")
                    .description("How many ticks to hold back for safe walk.")
                    .visible((s) -> !useBaritone.get() && safeWalk.get())
                    .defaultTo(3)
                    .range(0, 5)
                    .addTo(anchoring);
    public final BoolSetting differing =
            new BoolSetting.Builder()
                    .name("differing")
                    .description("Re-routes when certain conditions are met.")
                    .defaultTo(true)
                    .addTo(anchoring);
    public final FloatSetting differDistance =
            new FloatSetting.Builder()
                    .name("differ-distance")
                    .description("How close to a block you can be before re-routing.")
                    .defaultTo(0.75f)
                    .range(0.1f, 1.5f)
                    .addTo(anchoring);
    public final IntSetting anchorRange =
            new IntSetting.Builder()
                    .name("anchor-range")
                    .description("The range to anchor to blocks, by chunks.")
                    .defaultTo(5)
                    .range(2, 32)
                    .addTo(anchoring);
    public final IntSetting anchorResetDelay =
            new IntSetting.Builder()
                    .name("anchor-reset-delay")
                    .description("Delay between resetting the anchor.")
                    .defaultTo(10)
                    .range(5, 1200)
                    .addTo(anchoring);
    public final IntSetting anchorSortDelay =
            new IntSetting.Builder()
                    .name("anchor-sort-delay")
                    .description("Delay between re-sorting the anchor list.")
                    .defaultTo(10)
                    .range(1, 1200)
                    .addTo(anchoring);
    // Auto Swim
    private final GroupedSettings autoSwimGrouped = addGroup(new GroupedSettings("Auto Swim", "Automatically swim in liquids!"));
    public final BoolSetting autoSwim =
            new BoolSetting.Builder()
                    .name("auto-swim")
                    .description("Navigate out of water.")
                    .defaultTo(true)
                    .addTo(autoSwimGrouped);
    protected final IntSetting savingGraceDelay =
            new IntSetting.Builder()
                    .name("saving-grace-delay")
                    .description("How often to look for suitable land.")
                    .defaultTo(5)
                    .range(0, 20)
                    .addTo(autoSwimGrouped);
    protected final IntSetting savingGraceRadius =
            new IntSetting.Builder()
                    .name("saving-grace-radius")
                    .description("The distance around the selected position to look for suitable land.")
                    .defaultTo(64)
                    .range(16, 64)
                    .addTo(autoSwimGrouped);
    protected final IntSetting o2Radius =
            new IntSetting.Builder()
                    .name("o2-radius")
                    .description("The distance around the selected position to look for an opening of air.")
                    .defaultTo(24)
                    .range(8, 64)
                    .addTo(autoSwimGrouped);
    // Auto Return
    private final GroupedSettings autoReturnGrouped = addGroup(new GroupedSettings("Auto Return", "I miss home :("));
    public final BoolSetting autoReturn =
            new BoolSetting.Builder()
                    .name("auto-return")
                    .description("Return to a set position once out of materials.")
                    .defaultTo(true)
                    .addTo(autoReturnGrouped);
    public final BlockPosSetting returnPos =
            new BlockPosSetting.Builder()
                    .name("return-pos")
                    .description("The return 'home' position.")
                    .defaultTo(new ImBlockPos())
                    .addTo(autoReturnGrouped);
    public final BoolSetting returnOnIdle =
            new BoolSetting.Builder()
                    .name("return-on-idle")
                    .description("Returns if you idle around.")
                    .defaultTo(true)
                    .addTo(autoReturnGrouped);
    public final IntSetting idleWait =
            new IntSetting.Builder()
                    .name("idle-wait")
                    .description("How long to wait while idle.")
                    .defaultTo(90)
                    .range(1, 1200)
                    .addTo(autoReturnGrouped);
    public final BoolSetting returnOnEmpty =
            new BoolSetting.Builder()
                    .name("return-on-empty")
                    .description("Returns if you don't have a specific item in inventory.")
                    .defaultTo(true)
                    .addTo(autoReturnGrouped);
    public final ItemListSetting emptyCheck =
            new ItemListSetting.Builder()
                    .name("empty-check")
                    .description("Items to check for.")
                    .defaultTo(new ArrayList<>())
                    .addTo(autoReturnGrouped);
    public final FloatSetting homeDistance =
            new FloatSetting.Builder()
                    .name("home-distance")
                    .description("How close to home before stopping.")
                    .defaultTo(0.75f)
                    .range(0.1f, 1.5f)
                    .addTo(autoReturnGrouped);
    public final FloatSetting returnDistance =
            new FloatSetting.Builder()
                    .name("return-distance")
                    .description("How close to return position before stopping.")
                    .defaultTo(0.75f)
                    .range(0.1f, 1.5f)
                    .addTo(autoReturnGrouped);
    // Debug
    private final GroupedSettings sgDebug = addGroup(new GroupedSettings("Debug", "Funny settings"));
    public final IntSetting anchorBreakLimit =
            sgDebug.add(
                    new IntSetting.Builder()
                            .name("anchor-break-limit")
                            .description(".")
                            .defaultTo(256)
                            .range(1, 256)
                            .build());
    public final IntSetting placingLimit =
            sgDebug.add(
                    new IntSetting.Builder()
                            .name("placing-limit")
                            .description(".")
                            .defaultTo(256)
                            .range(1, 256)
                            .build());
    public final IntSetting anchorSoftCap =
            sgDebug.add(
                    new IntSetting.Builder()
                            .name("anchor-soft-cap")
                            .description(".")
                            .defaultTo(256)
                            .range(1, 256)
                            .build());
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    public int lastLiquidPlace = 0;
    public int swapTimer = 0;
    public int placeTimer;
    public int blocksPlacedThisSec = 0;
    // Swimming ?
    protected BlockPos airOpeningTemp;
    protected boolean wasSwimmingUp = false;
    protected BlockPos savingGrace;
    protected int savingGraceTimer = 0;
    // Sleeping
    protected boolean sleepJob = false;
    protected int sleepAttemptTimer = 0;
    protected BlockPos sleepReturnTo;
    protected long tickTimestamp = -1;
    int anchorRefreshTimer = 0;
    int anchorSortTimer = 0;
    private boolean pauseTillRefilled = false;
    private int anchorResetTimer;
    private BlockPos returnTo;
    private int lastSecond = 0;
    private final BaritoneMove baritoneMove = new BaritoneMove();
    private final AdvancedMove vanillaMove = new AdvancedMove();
    private final VanillaMove trueVanillaMove = new VanillaMove();
    private BlockPos lastBlockPos;
    private int sameAsLastBlock = 0;
    public int lastSwapTimer = 0;

    public Printer() {
        super(Categories.WORLD, "printer", "Places litematica schematics, designed for mapart.");
        PrinterUtils.PRINTER = this;
    }

    @Override
    public void enabled() {
        super.enabled();
        if (MC.player == null || MC.level == null || MC.gameMode == null) {
            return;
        }

        anchorToSort.clear();
        toSort.clear();
        fading.clear();
        anchoringTo.set(0, -999, 0);
        lastPlacedBlock.set(0, 0, 0);
        pauseTillRefilled = false;
        baritone.getPathingBehavior().cancelEverything();
        MC.options.keyUp.setDown(false);
        if (wasSwimmingUp) {
            wasSwimmingUp = false;
            MC.options.keyJump.setDown(false);
        }
        airOpeningTemp = null;
        savingGrace = null;
        sleepJob = false;
        sleepAttemptTimer = 0;
        sameAsLastBlock = 0;
        sleepReturnTo = null;

        lastBlockPos = BlockPos.ZERO;

        anchorRefreshTimer = 749;
        PlacingManager.reorderChunks(anchorRange.get());

        baritoneMove.cancel(BlockPos.ZERO);
        vanillaMove.cancel(BlockPos.ZERO);
    }

    @Override
    protected void disabled() {
        super.disabled();
        baritone.getPathingBehavior().cancelEverything();
    }

    @Override
    public boolean controlsHotbar() {
        return true;
    }

    @Override
    public boolean inUse() {
        return swapTimer > 0;
    }

    @Override
    public void update() {
        if (MC.player == null || MC.level == null || MC.gameMode == null) {
            return;
        }

        int second = LocalDateTime.now().getSecond();

        if (lastSecond != second) {
            lastSecond = second;
            blocksPlacedThisSec = 0;
        }

        lastLiquidPlace--;

        tickTimestamp = System.currentTimeMillis();

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) {
            active(!active());
            return;
        }

        if (swapTimer > 0) {
            swapTimer--;
        }

        containedColors.clear();
        containedBlocks.clear();

        for (ItemStack stack : MC.player.getInventory().getNonEquipmentItems()) {
            if (InventoryUtils.IS_BLOCK.test(stack)) {
                if (strictNoColor.get()) {
                    containedBlocks.add(stack.getItem());
                } else if (blockExclusion.value().stream()
                        .noneMatch((block -> stack.getItem() == block.asItem()))) {
                    containedColors.add(McDataCache.getColor(stack));
                }
            }
        }

        if (autoSwim.get()) {
            StuckFixManager.runSwimTask();

            if (MC.player.onGround()) {
                if (wasSwimmingUp) {
                    MC.options.keyJump.setDown(false);
                    MC.options.keyUp.setDown(false);
                    wasSwimmingUp = false;
                    savingGrace = null;
                }
            }

            if (StuckFixManager.shouldCancelForSwimmingTask()) {
                return;
            }
        }

        if (autoReturn.get() && !pauseTillRefilled) {
            if (returnOnIdle.get() && sameAsLastBlock >= idleWait.get()) {
                returnTo = MC.player.blockPosition();
                pauseTillRefilled = true;
                sameAsLastBlock = 0;
                baritone.getPathingBehavior().cancelEverything();
                NotificationManager.get().addNotification("Printer", "Refilling inventory.", NotificationEvent.Type.SUCCESS, 10000L);
                return;
            }

            if (returnOnEmpty.get() && InventoryUtils.findAnySlot(emptyCheck.value()) == -1) {
                returnTo = MC.player.blockPosition();
                pauseTillRefilled = true;
                baritone.getPathingBehavior().cancelEverything();
                NotificationManager.get().addNotification("Printer", "Refilling inventory.", NotificationEvent.Type.SUCCESS, 10000L);
                return;
            }

            if (MC.player.blockPosition().equals(lastBlockPos)) {
                sameAsLastBlock++;
            } else {
                lastBlockPos = MC.player.blockPosition();
                sameAsLastBlock = 0;
            }

            if (lastSwapTimer != swapTimer) {
                lastSwapTimer = swapTimer;
            }
        }

        if (pauseTillRefilled && autoReturn.get()) {
            if (MC.player.getInventory().getFreeSlot() == -1) {
                returnMovement(returnTo);
                if (Math.abs(MC.player.position().distanceTo(returnTo.getCenter())) <= returnDistance.get()) {
                    if (MC.player.onGround()) {
                        pauseTillRefilled = false;
                    }

                    anchorRefreshTimer = 749;
                    baritoneMove.cancel(returnTo);
                }
            } else {
                System.out.println("Refill in progress.");
                returnMovement(returnPos.value().get());

                anchorRefreshTimer = 749;

                if (Math.abs(MC.player.position().distanceTo(returnPos.value().get().getCenter())) <= homeDistance.get()) {
                    System.out.println("At home.");
                    baritoneMove.cancel(returnPos.value().get());
                }
            }

            return;
        }

        if (anchor.get()) {
            if (swapTimer <= 0) {
                if ((anchoringTo.getY() != -999 && (isDiffered(anchoringTo) || BlockUtils.isNotAir(anchoringTo) || BlockUtils.hasEntitiesInside(anchoringTo)))
                        || anchorResetTimer >= anchorResetDelay.get()) {
                    anchoringTo.set(0, -999, 0);


                    anchorResetTimer = 1;
                    if (useBaritone.get()) {
                        if (baritone.getPathingBehavior().hasPath())
                            baritone.getPathingBehavior().cancelEverything();
                    } else {
                        vanillaMove.cancel(BlockPos.ZERO);
                    }
                } else if (anchoringTo.getY() == -999) {
                    if (useBaritone.get()) {
                        if (baritone.getPathingBehavior().hasPath())
                            baritone.getPathingBehavior().cancelEverything();
                    } else {
                        vanillaMove.cancel(BlockPos.ZERO);
                    }
                } else {
                    if (anchorSortDelay.get() >= anchorSortTimer) {
                        anchorToSort.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

                        anchorSortTimer = 0;
                    }

                    anchorSortTimer++;

                    if (useBaritone.get()) {
                        if (!baritone.getPathingBehavior().hasPath())
                            baritoneMove.tick(anchoringTo);
                    } else {
                        vanillaMove.tick(anchoringTo);
                    }
                }

                anchorRefreshTimer++;

                if (anchoringTo.getY() == -999) {
                    if (anchorRefreshTimer >= 60) {
                        BlockPos.MutableBlockPos srcBlock = new BlockPos.MutableBlockPos(0, 0, 0);

                        List<int[]> anchoredBlocks = PrinterUtils.findNearBlocksByChunk(MC.player.blockPosition().mutable().setY(yLevel.get()).immutable(),
                                anchorRange.get(),
                                (pos) -> {
                                    srcBlock.set(pos[0], yLevel.get(), pos[2]);

                                    BlockState blockState = MC.level.getBlockState(srcBlock);
                                    BlockState required = worldSchematic.getBlockState(srcBlock);

                                    if (blockState.isAir() && !required.isAir() && !BlockUtils.hasEntitiesInside(srcBlock)) {
                                        return ((!strictNoColor.get()
                                                && containedColors.contains(
                                                McDataCache.getColor(required.getBlock().asItem())))
                                                || (strictNoColor.get()
                                                && containedBlocks.contains(required.getBlock().asItem())))
                                                && !isDiffered(srcBlock);
                                    }

                                    return false;
                                });

                        anchorToSort.clear();

                        anchoredBlocks.sort(Comparator.comparingDouble(value -> value[0]));
                        anchoredBlocks.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

                        int maxAnchoredBlocks = 0;

                        int anchorLimit = anchorSoftCap.get();

                        for (int[] posVec : anchoredBlocks) {
                            if (maxAnchoredBlocks >= anchorLimit) {
                                break;
                            }

                            maxAnchoredBlocks++;
                            anchorToSort.add(posVec);
                        }

                        anchoredBlocks.clear();

                        anchorRefreshTimer = 0;
                        anchorSortTimer = 0;
                    }

                    if (!anchorToSort.isEmpty()) {
                        anchoringTo = anchoringTo.set(anchorToSort.get(0)[0], anchorToSort.get(0)[1], anchorToSort.get(0)[2]);
                        anchorToSort.remove(0);
                    }
                }

                anchorResetTimer++;
            } else {
                MC.options.keyUp.setDown(false);
                if (baritone.getPathingBehavior().hasPath())
                    baritone.getPathingBehavior().cancelEverything();
            }
        }

        toSort.clear();

        if (placeTimer >= delay.get()) {
            if (!passesChecks()) {
                return;
            }

            PlacingManager.tryPlacingBlocks();

        } else placeTimer++;

        if (!(lastPlacedBlock.getX() == 0 && lastPlacedBlock.getY() == 0 && lastPlacedBlock.getZ() == 0)) {
            fading.add(new FadeEntry(lastPlacedBlock.immutable(), System.currentTimeMillis()));
            lastPlacedBlock.set(0, 0, 0);
        }
    }

    public boolean passesChecks() {
        if (MC.player == null) {
            return false;
        }

        if (onlyOnGround.get() && !MC.player.onGround()) {
            return false;
        }

        if (MC.player.isUsingItem()) {
            return false;
        }

        return !notInLiquid.get() || !MC.player.isInLiquid();
    }

    public boolean isDiffered(BlockPos pos) {
        if (MC.player == null || !differing.get()) return false;

        return MathUtils.xzDistanceBetween(MC.player.getEyePosition(), pos) <= differDistance.get();
    }

    public void returnMovement(BlockPos pos) {
        trueVanillaMove.tick(pos);
    }

    @EventListener
    public void onRender3D(OnRender event) {
        if (!renderEnabled.get()) return;
        if (MC.level == null || MC.player == null) return;

        RenderSystem.lineWidth(renderLineWidth.get());
        PoseStack.Pose pose = event.poseStack.last();

        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        if (renderAnchor.get() && anchor.get()) {
            BlockPos ap = anchoringTo;
            if (ap != null && ap.getY() != -999) {
                float[] ac = anchorColor.get();
                float tx = (float) ((ap.getX() + 0.5) - camX);
                float ty = (float) ((ap.getY() + 0.5) - camY);
                float tz = (float) ((ap.getZ() + 0.5) - camZ);
                Render3DUtil.drawTracer(event.lines, pose, tx, ty, tz, ac[0], ac[1], ac[2], ac[3]);

                AABB bb = new AABB(ap);
                AABB rel = bb.move(-camX, -camY, -camZ);
                Render3DUtil.drawBoxOutline(event.lines, pose, rel, ac[0], ac[1], ac[2], ac[3]);
                Render3DUtil.drawBoxInner(event.quads, pose, rel, ac[0], ac[1], ac[2], ac[3] * 0.15f);
            }
        }

        if (renderQueue.get()) {
            float[] qc = queueColor.get();
            int count = 0;
            for (int[] posVec : toSort) {
                if (count++ >= renderQueueLimit.get()) break;
                BlockPos p = new BlockPos(posVec[0], posVec[1], posVec[2]);
                AABB bb = new AABB(p);
                AABB rel = bb.move(-camX, -camY, -camZ);
                Render3DUtil.drawBoxOutline(event.lines, pose, rel, qc[0], qc[1], qc[2], qc[3]);
            }

            long now = System.currentTimeMillis();
            for (int i = fading.size() - 1; i >= 0; i--) {
                FadeEntry fe = fading.get(i);
                float t = (float) ((now - fe.startMs) / (double) fadeDurationMs.get());
                if (t >= 1.0f) {
                    fading.remove(i);
                    continue;
                }
                float scale = 1.0f - t;
                float minH = 0.05f;
                float h = Math.max(minH, scale);

                BlockPos bp = fe.pos;
                float minX = (float) (bp.getX() - camX);
                float minY = (float) (bp.getY() - camY);
                float minZ = (float) (bp.getZ() - camZ);
                float maxX = minX + 1.0f;
                float maxY = minY + h;
                float maxZ = minZ + 1.0f;

                float aEdge = qc[3] * (1.0f - t);
                float aFace = aEdge * 0.25f;

                Render3DUtil.drawBoxOutline(event.lines, pose, minX, minY, minZ, maxX, maxY, maxZ, qc[0], qc[1], qc[2], aEdge);
                Render3DUtil.drawBoxInner(event.quads, pose, minX, minY, minZ, maxX, maxY, maxZ, qc[0], qc[1], qc[2], aFace);
            }
        }
    }
}
