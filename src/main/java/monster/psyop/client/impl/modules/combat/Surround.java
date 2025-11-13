package monster.psyop.client.impl.modules.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Surround extends Module {
    private final Map<BlockPos, VisualType> renderMap = new ConcurrentHashMap<>();
    private final List<BlockPos> savedBlocks = new CopyOnWriteArrayList<>();

    public FloatSetting placeDistance = new FloatSetting.Builder()
            .name("place-distance")
            .description("The max distance to place blocks.")
            .defaultTo(3.75f)
            .range(3.2f, 5.0f)
            .addTo(coreGroup);
    public IntSetting blocksPerTick = new IntSetting.Builder()
            .name("blocks-per-tick")
            .description("How many blocks to place per tick.")
            .defaultTo(2)
            .range(1, 8)
            .addTo(coreGroup);
    public IntSetting tickDelay = new IntSetting.Builder()
            .name("tick-delay")
            .description("Delay in ticks per cycle.")
            .defaultTo(0)
            .range(0, 10)
            .addTo(coreGroup);
    public ItemListSetting fallback = new ItemListSetting.Builder()
            .name("fallback-blocks")
            .description("The blocks to fall back to if you run out of obsidian.")
            .defaultTo(List.of(Items.NETHERITE_BLOCK))
            .addTo(coreGroup);
    public BoolSetting protectUnder = new BoolSetting.Builder()
            .name("protect-under")
            .description("Places obsidian under the primary blocks if they are vulnerable.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting noConflict = new BoolSetting.Builder()
            .name("no-conflict")
            .description("Breaks crystals that could prevent the block from being replaced.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting autoReplace = new BoolSetting.Builder()
            .name("auto-replace")
            .description("Attempts to replace the crystal with obsidian.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting saveReplaced = new BoolSetting.Builder()
            .name("save-replaced")
            .description("Saves the position and makes sure they're not broken.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting markConflicted = new BoolSetting.Builder()
            .name("mark-as-saved")
            .description("Saves the position and makes sure they're not broken.")
            .defaultTo(true)
            .addTo(coreGroup);
    public IntSetting crystalTickDelay = new IntSetting.Builder()
            .name("break-delay")
            .description("The ticks between breaking crystals.")
            .defaultTo(3)
            .range(0, 40)
            .addTo(coreGroup);
    public BoolSetting onGround = new BoolSetting.Builder()
            .name("on-ground")
            .description("Only place blocks on the ground.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting useItem = new BoolSetting.Builder()
            .name("use-item")
            .description("Pauses when using an item.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting render = new BoolSetting.Builder()
            .name("render")
            .description("Render block placements.")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting placedColor = new ColorSetting.Builder()
            .name("placed")
            .defaultTo(new float[]{0.0f, 0.5f, 0.0f, 0.24f})
            .addTo(coreGroup);
    public ColorSetting waitingColor = new ColorSetting.Builder()
            .name("waiting")
            .defaultTo(new float[]{1.0f, 0.65f, 0.0f, 0.24f})
            .addTo(coreGroup);
    public ColorSetting conflictingColor = new ColorSetting.Builder()
            .name("conflicting")
            .defaultTo(new float[]{1.0f, 0.0f, 0.0f, 0.24f})
            .addTo(coreGroup);
    public ColorSetting failColor = new ColorSetting.Builder()
            .name("fail")
            .defaultTo(new float[]{1.0f, 0.0f, 0.0f, 0.24f})
            .addTo(coreGroup);

    private BlockPos currentHole = BlockPos.ZERO;
    private int updateTicks = 0;
    private int breakTicks = 0;

    private static final Direction[] HORIZONTALS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public Surround() {
        super(Categories.COMBAT, "surround", "Surrounds your feet with blocks.");
    }

    @Override
    public void enabled() {
        super.enabled();
        this.updateTicks = 0;
        this.renderMap.clear();
        this.savedBlocks.clear();
    }

    @Override
    public void disabled() {
        super.disabled();
        this.renderMap.clear();
        this.savedBlocks.clear();
    }

    @EventListener
    public void onTick(OnTick event) {
        if (updateTicks > 0) {
            updateTicks--;
            return;
        }

        if (breakTicks > 0) breakTicks--;
        placeBlocks();
    }

    @EventListener
    public void onRender(OnRender event) {
        if (!render.get()) return;

        PoseStack.Pose pose = event.poseStack.last();
        Vec3 camPos = MC.gameRenderer.getMainCamera().getPosition();

        for (Map.Entry<BlockPos, VisualType> entry : renderMap.entrySet()) {
            float[] color = getColorForVisualType(entry.getValue());
            renderBlock(event, pose, camPos, entry.getKey(), color);
        }
    }

    private float[] getColorForVisualType(VisualType type) {
        return switch (type) {
            case PLACED -> placedColor.get();
            case WAITING -> waitingColor.get();
            case CONFLICTING -> conflictingColor.get();
            case FAIL -> failColor.get();
        };
    }

    private void renderBlock(OnRender event, PoseStack.Pose pose, Vec3 camPos, BlockPos pos, float[] color) {
        AABB box = new AABB(pos);
        Render3DUtil.drawBoxInner(event.lines, pose, box, camPos, color);
        Render3DUtil.drawBoxOutline(event.lines, pose, box, camPos,
                Math.min(1.0f, color[0] * 1.2f),
                Math.min(1.0f, color[1] * 1.2f),
                Math.min(1.0f, color[2] * 1.2f),
                Math.min(1.0f, color[3] * 1.5f));
    }

    public void placeBlocks() {
        if (MC.player == null) return;
        if (onGround.get() && !MC.player.onGround()) return;
        if (useItem.get() && MC.player.isUsingItem()) return;

        int blocksPlaced = 0;
        updateTicks = tickDelay.get();

        BlockPos original = MC.player.blockPosition();
        if (!currentHole.equals(original)) {
            currentHole = original;
            savedBlocks.clear();
        }

        renderMap.clear();

        boolean isNoConflict = noConflict.get();
        boolean isMarkConflicted = markConflicted.get();
        boolean isAutoReplace = autoReplace.get();

        for (Direction direction : HORIZONTALS) {
            BlockPos offset = original.relative(direction);

            if (isNoConflict) {
                List<EndCrystal> imposedCrystals = getImposedCrystals(offset);
                for (EndCrystal crystal : imposedCrystals) {
                    BlockPos pos = crystal.blockPosition();

                    if (isMarkConflicted) {
                        if (!savedBlocks.contains(pos))
                            savedBlocks.add(pos);
                    }

                    if (breakTicks <= 0) {
                        MC.getConnection().send(ServerboundInteractPacket.createAttackPacket(crystal, false));
                        MC.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                        breakTicks = crystalTickDelay.get();
                    }

                    if (isAutoReplace) {
                        Optional<net.minecraft.world.item.ItemStack> itemResult = findObsidianOrFallback();
                        if (itemResult.isPresent()) {
                            if (saveReplaced.get() && !savedBlocks.contains(pos))
                                savedBlocks.add(pos);
                            if (placeBlock(itemResult.get(), pos))
                                blocksPlaced++;
                        }
                    }
                }
            }

            if (!canExplode(offset)) {
                renderMap.put(offset, VisualType.PLACED);
                continue;
            }

            if (blocksPlaced >= blocksPerTick.get()) {
                renderMap.put(offset, VisualType.WAITING);
                continue;
            }

            // Place block under player if needed
            BlockPos underPos = original.below();
            if (isReplaceable(underPos)) {
                if (place(underPos)) blocksPlaced++;
            }

            if (isSurroundMissing()) {
                if (blocksPlaced >= blocksPerTick.get()) {
                    break;
                }

                if (place(offset)) blocksPlaced++;

                if (protectUnder.get()) {
                    BlockPos underOffset = offset.below();
                    if (place(underOffset)) {
                        if (saveReplaced.get() && !savedBlocks.contains(underOffset)) {
                            savedBlocks.add(underOffset);
                        }
                        blocksPlaced++;
                    }
                }
            }
        }

        if (saveReplaced.get() && blocksPlaced < blocksPerTick.get()) {
            for (BlockPos pos : savedBlocks) {
                if (!canExplode(pos)) {
                    renderMap.put(pos, VisualType.PLACED);
                    continue;
                }

                if (blocksPlaced >= blocksPerTick.get()) {
                    renderMap.put(pos, VisualType.WAITING);
                    continue;
                }

                if (place(pos)) blocksPlaced++;
            }
        }
    }

    public boolean place(BlockPos pos) {
        if (canPlace(pos)) {
            Optional<net.minecraft.world.item.ItemStack> itemResult = findObsidianOrFallback();
            if (itemResult.isEmpty()) {
                renderMap.put(pos, VisualType.FAIL);
                return false;
            }

            if (placeBlock(itemResult.get(), pos)) {
                renderMap.put(pos, VisualType.PLACED);
                return true;
            } else {
                if (hasEntitiesInside(pos)) {
                    renderMap.put(pos, VisualType.CONFLICTING);
                } else {
                    renderMap.put(pos, VisualType.FAIL);
                }
            }
        } else {
            if (hasEntitiesInside(pos)) {
                renderMap.put(pos, VisualType.CONFLICTING);
            } else {
                renderMap.put(pos, VisualType.FAIL);
            }
        }
        return false;
    }

    // Utility methods
    private List<EndCrystal> getImposedCrystals(BlockPos pos) {
        List<EndCrystal> crystals = new ArrayList<>();
        AABB box = new AABB(pos);

        if (MC.level != null) {
            for (EndCrystal crystal : MC.level.getEntitiesOfClass(EndCrystal.class, box.inflate(2))) {
                if (crystal.getBoundingBox().intersects(box)) {
                    crystals.add(crystal);
                }
            }
        }

        return crystals;
    }

    private boolean canExplode(BlockPos pos) {
        if (MC.level == null) return true;
        return MC.level.getBlockState(pos).getBlock().defaultDestroyTime() < 50; // Not blast resistant
    }

    private boolean isReplaceable(BlockPos pos) {
        if (MC.level == null) return false;
        return MC.level.getBlockState(pos).canBeReplaced();
    }

    private boolean isSurroundMissing() {
        BlockPos playerPos = MC.player.blockPosition();
        for (Direction dir : HORIZONTALS) {
            if (isReplaceable(playerPos.relative(dir))) {
                return true;
            }
        }
        return false;
    }

    private boolean canPlace(BlockPos pos) {
        if (MC.level == null) return false;
        return MC.level.getBlockState(pos).canBeReplaced() &&
                MC.level.getWorldBorder().isWithinBounds(pos) &&
                pos.getY() >= MC.level.getMinY() &&
                pos.getY() <= MC.level.getMaxY();
    }

    private boolean hasEntitiesInside(BlockPos pos) {
        if (MC.level == null) return false;
        AABB box = new AABB(pos);
        return !MC.level.getEntities(null, box).isEmpty();
    }

    private Optional<net.minecraft.world.item.ItemStack> findObsidianOrFallback() {
        // Check hotbar first
        for (int i = 0; i < 9; i++) {
            net.minecraft.world.item.ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.getItem() == Items.OBSIDIAN) {
                return Optional.of(stack);
            }
        }

        // Check fallback items
        for (int i = 0; i < 9; i++) {
            net.minecraft.world.item.ItemStack stack = MC.player.getInventory().getItem(i);
            if (fallback.value().contains(stack.getItem())) {
                return Optional.of(stack);
            }
        }

        return Optional.empty();
    }

    private boolean placeBlock(net.minecraft.world.item.ItemStack stack, BlockPos pos) {
        if (MC.player == null || MC.level == null) return false;

        double distance = Math.sqrt(MC.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        if (distance > placeDistance.get()) return false;

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (MC.player.getInventory().getItem(i) == stack) {
                slot = i;
                break;
            }
        }
        if (slot == -1) return false;

        int prevSlot = MC.player.getInventory().getSelectedSlot();
        MC.player.getInventory().setSelectedSlot(slot);

        // Try to place the block
        boolean success = false;
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (Direction dir : directions) {
            BlockPos neighbor = pos.relative(dir);
            if (!MC.level.getBlockState(neighbor).isAir()) {
                // Use MC's interaction manager to place the block
                if (MC.gameMode != null) {
                    success = MC.gameMode.useItemOn(
                            MC.player,
                            InteractionHand.MAIN_HAND,
                            new net.minecraft.world.phys.BlockHitResult(
                                    new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                                    dir.getOpposite(),
                                    pos,
                                    false
                            )
                    ).consumesAction();
                    if (success) break;
                }
            }
        }

        // Restore previous slot
        MC.player.getInventory().setSelectedSlot(prevSlot);

        return success;
    }

    public enum VisualType {
        PLACED,
        WAITING,
        CONFLICTING,
        FAIL
    }
}