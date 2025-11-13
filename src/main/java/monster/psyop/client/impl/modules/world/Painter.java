package monster.psyop.client.impl.modules.world;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BlockListSetting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.rendering.ContractingBlock;
import monster.psyop.client.framework.rendering.RenderHelper;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class Painter extends Module {
    public final IntSetting dedicatedSlot = new IntSetting.Builder()
            .name("dedicated-slot")
            .defaultTo(7)
            .range(0, 8)
            .addTo(coreGroup);
    public BlockListSetting cover = new BlockListSetting.Builder()
            .name("cover")
            .defaultTo(new ArrayList<>(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE)))
            .addTo(coreGroup);
    public BlockListSetting paint = new BlockListSetting.Builder()
            .name("paint")
            .defaultTo(new ArrayList<>(List.of(Blocks.DIAMOND_BLOCK)))
            .addTo(coreGroup);
    public GroupedSettings renderingGroup = addGroup(new GroupedSettings("rendering", "Rendering"));
    public BoolSetting render = new BoolSetting.Builder()
            .name("render")
            .defaultTo(true)
            .addTo(renderingGroup);
    public BoolSetting outline = new BoolSetting.Builder()
            .name("outline")
            .defaultTo(true)
            .addTo(renderingGroup);
    public IntSetting renderTime = new IntSetting.Builder()
            .name("render-time")
            .defaultTo(2500)
            .range(500, 30000)
            .addTo(renderingGroup);
    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{0.0f, 1.0f, 0.0f, 0.4f})
            .addTo(renderingGroup);

    public List<ContractingBlock> contractingBlocks = new ArrayList<>();

    public Painter() {
        super(Categories.WORLD, "painter", "Paints terrain with a different block.");
    }

    @Override
    public void update() {
        RenderHelper.removeExpiredContracting(contractingBlocks);

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        List<int[]> blocks = new ArrayList<>();
        for (int y = -3; y <= 3; y++) {
            List<int[]> blockVecs = BlockUtils.findNearBlocksByRadius(MC.player.blockPosition().offset(0, y, 0).mutable(), 5, (vecPos) -> {
                mutableBlockPos.set(vecPos[0], vecPos[1], vecPos[2]);

                return BlockUtils.isExposed(mutableBlockPos)
                        && cover.value().contains(BlockUtils.getState(mutableBlockPos).getBlock());
            });
            blocks.addAll(blockVecs);
        }

        List<int[]> placeable = new ArrayList<>();

        for (int[] vec : blocks) {
            mutableBlockPos.set(vec[0], vec[1], vec[2]);

            for (Direction dir : Direction.values()) {
                mutableBlockPos.set(vec[0] + dir.getStepX(), vec[1] + dir.getStepY(), vec[2] + dir.getStepZ());
                if (BlockUtils.isReplaceable(mutableBlockPos)) {
                    if (BlockUtils.canPlace(mutableBlockPos)) {
                        placeable.add(new int[]{vec[0] + dir.getStepX(), vec[1] + dir.getStepY(), vec[2] + dir.getStepZ()});
                    }
                }
            }

        }

        if (blocks.isEmpty()) {
            return;
        }

        placeable.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

        mutableBlockPos.set(placeable.get(0)[0], placeable.get(0)[1], placeable.get(0)[2]);

        int slot = InventoryUtils.findAnySlotB(paint.value());

        if (slot == -1) {
            return;
        }

        InventoryUtils.swapSlot(dedicatedSlot.get());

        List<Item> block2Item = new ArrayList<>();

        for (Block b : paint.value()) {
            block2Item.add(b.asItem());
        }

        if (!block2Item.contains(MC.player.getMainHandItem().getItem())) {
            InventoryUtils.swapToHotbar(slot, dedicatedSlot.get());
            return;
        }

        MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, BlockUtils.getSafeHitResult(mutableBlockPos));
        contractingBlocks.add(new ContractingBlock(mutableBlockPos.immutable(), System.currentTimeMillis(), renderTime.get()));
    }

    @EventListener
    public void onRender(OnRender event) {
        PoseStack.Pose pose = event.poseStack.last();

        for (ContractingBlock block : contractingBlocks) {
            if (!block.isExpired()) {
                block.render(pose, event.quads, event.lines, outline.get(), color.get());
            }
        }
    }
}
