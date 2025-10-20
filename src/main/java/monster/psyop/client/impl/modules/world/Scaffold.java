package monster.psyop.client.impl.modules.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Scaffold extends Module {
    public GroupedSettings sgDefault = addGroup(new GroupedSettings("general", "General settings"));
    public BlockListSetting whitelist = new BlockListSetting.Builder()
            .name("whitelist")
            .description("Only places blocks from this list.")
            .defaultTo(List.of())
            .addTo(sgDefault);
    public IntSetting dedicatedSlot = new IntSetting.Builder()
            .name("dedicated-slot")
            .description("Hotbar slot to use for placement (0-8)")
            .range(0, 8)
            .defaultTo(7)
            .addTo(sgDefault);
    public IntSetting blocksPerTick = new IntSetting.Builder()
            .name("blocks-per-tick")
            .description("How many blocks to place per tick")
            .range(1, 8)
            .defaultTo(1)
            .addTo(sgDefault);
    public IntSetting radius = new IntSetting.Builder()
            .name("radius")
            .description("XZ radius to search for place positions")
            .range(1, 5)
            .defaultTo(3)
            .addTo(sgDefault);
    public FloatSetting placeDistance = new FloatSetting.Builder()
            .name("place-distance")
            .description("Max distance to the hit position for placing")
            .range(3.2f, 5.0f)
            .defaultTo(3.75f)
            .addTo(sgDefault);
    public IntSetting depth = new IntSetting.Builder()
            .name("depth")
            .description("How far down to search for positions")
            .range(1, 5)
            .defaultTo(3)
            .addTo(sgDefault);
    public GroupedSettings sgLocks = addGroup(new GroupedSettings("locks", "Placement locks"));
    public BoolSetting lockToY = new BoolSetting.Builder()
            .name("lock-to-y")
            .description("Only place at a specific Y level")
            .defaultTo(false)
            .addTo(sgLocks);
    public IntSetting yLevel = new IntSetting.Builder()
            .name("y-level")
            .description("Y level to lock placement to")
            .range(-64, 320)
            .defaultTo(319)
            .addTo(sgLocks);
    public BoolSetting orAbove = new BoolSetting.Builder()
            .name("or-above")
            .description("Allow placement at Y-level or above")
            .defaultTo(true)
            .addTo(sgLocks);
    public GroupedSettings sgRender = addGroup(new GroupedSettings("render", "Rendering"));
    public BoolSetting rendering = new BoolSetting.Builder()
            .name("rendering")
            .description("Render a box when placing blocks")
            .defaultTo(true)
            .addTo(sgRender);
    public ColorSetting renderColor = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{195 / 255f, 126 / 255f, 234 / 255f, 0.44f})
            .addTo(sgRender);
    public IntSetting fadeTime = new IntSetting.Builder()
            .name("fade-time")
            .description("How long placed boxes take to fade (ticks)")
            .range(20, 1000)
            .defaultTo(160)
            .addTo(sgRender);

    private final List<PlacedBox> recent = new ArrayList<>();

    public Scaffold() {
        super(Categories.WORLD, "scaffold", "Automatically places blocks under/around your feet.");
    }

    @EventListener
    public void onTick(OnTick.Pre e) {
        if (MC.player == null || MC.gameMode == null) return;

        if (!recent.isEmpty()) {
            recent.removeIf(pb -> ++pb.age >= fadeTime.get());
        }

        int toPlace = Math.max(1, blocksPerTick.get());
        for (int i = 0; i < toPlace; i++) {
            BlockPos next = getNextBlock();
            if (next == null) break;

            List<Item> allowed = whitelist.value().stream().map(Block::asItem).collect(Collectors.toList());
            int slot = InventoryUtils.findAnySlot(allowed);
            if (slot == -1) break;

            int hotbarOffset = InventoryUtils.getHotbarOffset();
            if (!allowed.contains(MC.player.getMainHandItem().getItem()) && (slot < hotbarOffset || slot > hotbarOffset + 8)) {
                InventoryUtils.swapToHotbar(slot, dedicatedSlot.get());

                return;
            }
            InventoryUtils.swapSlot(dedicatedSlot.get());

            if (BlockUtils.canPlace(next, placeDistance.get())) {
                MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, BlockUtils.getSafeHitResult(next));
                if (rendering.get()) recent.add(new PlacedBox(next, renderColor.get().clone()));
            }
        }
    }

    private boolean canPlaceAt(BlockPos pos) {
        if (MC.player == null) return false;
        if (lockToY.get()) {
            if (orAbove.get()) {
                if (pos.getY() < yLevel.get()) return false;
            } else if (pos.getY() != yLevel.get()) return false;
        }
        return BlockUtils.canPlace(pos, placeDistance.get());
    }

    private BlockPos getNextBlock() {
        if (MC.player == null) return null;
        BlockPos below = MC.player.blockPosition().relative(Direction.DOWN);
        if (canPlaceAt(below)) return below;

        List<int[]> candidates = new ArrayList<>();
        BlockPos base = MC.player.getOnPos();
        for (int dy = 0; dy <= depth.get(); dy++) {
            int y = base.getY() - dy;
            for (int dx = -radius.get(); dx <= radius.get(); dx++) {
                for (int dz = -radius.get(); dz <= radius.get(); dz++) {
                    BlockPos p = new BlockPos(base.getX() + dx, y, base.getZ() + dz);
                    if (canPlaceAt(p)) candidates.add(new int[]{p.getX(), p.getY(), p.getZ()});
                }
            }
        }
        if (candidates.isEmpty()) return null;
        candidates.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);
        int[] first = candidates.get(0);
        return new BlockPos(first[0], first[1], first[2]);
    }

    @EventListener
    public void onRender3D(OnRender event) {
        if (!rendering.get() || recent.isEmpty() || MC.player == null) return;
        RenderSystem.lineWidth(1.5f);
        PoseStack ps = new PoseStack();
        PoseStack.Pose pose = ps.last();

        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();

        for (PlacedBox pb : recent) {
            float life = 1.0f - (pb.age / Math.max(1.0f, (float) fadeTime.get()));
            float[] c = pb.color;
            float a = Math.max(0.0f, Math.min(1.0f, c[3] * life));

            Render3DUtil.drawBlockBoxFaces(event.quads, pose, pb.pos, cam, 0.0f, c[0], c[1], c[2], a * 0.35f);
            Render3DUtil.drawBlockBoxEdges(event.lines, pose, pb.pos, cam, 0.0f, c[0], c[1], c[2], a);
        }
    }

    private static class PlacedBox {
        final BlockPos pos;
        final float[] color;
        int age = 0;

        PlacedBox(BlockPos pos, float[] color) {
            this.pos = pos.immutable();
            this.color = color;
        }
    }
}