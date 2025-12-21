package monster.psyop.client.impl.modules.world.printer;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import monster.psyop.client.Psyop;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.McDataCache;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import monster.psyop.client.utility.blocks.PlaceableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static monster.psyop.client.Psyop.MC;

public class PlacingManager {
    public static List<int[]> chunkScanningOrder = new ArrayList<>();

    public static void reorderChunks(int range) {
        chunkScanningOrder.clear();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                chunkScanningOrder.add(new int[]{x, z});
            }
        }

        chunkScanningOrder.sort(Comparator.comparingInt(ints -> Math.abs(ints[0]) + Math.abs(ints[1])));
    }

    public static boolean sortPlaceableBlocks() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null || MC.level == null || MC.player == null) {
            return false;
        }

        List<int[]> printerBlocks = new ArrayList<>(PrinterUtils.PRINTER.placeRadius.get() * PrinterUtils.PRINTER.placeRadius.get());

        if (!PrinterUtils.PRINTER.anchor.get()) {
            for (int i = -PrinterUtils.PRINTER.placeRadius.get(); i <= PrinterUtils.PRINTER.placeRadius.get(); i++) {
                printerBlocks.addAll(getBlocksForYLevel(worldSchematic, MC.player.getBlockY() + i));
            }
        } else {
            printerBlocks.addAll(getBlocksForYLevel(worldSchematic, PrinterUtils.PRINTER.yLevel.get()));
        }

        PrinterUtils.PRINTER.toSort.clear();

        int printerMaxSorting = 0;

        printerBlocks.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

        int placingLimit = PrinterUtils.PRINTER.placingLimit.get();

        for (int[] posVec : printerBlocks) {
            if (printerMaxSorting > placingLimit) {
                break;
            }

            printerMaxSorting++;
            PrinterUtils.PRINTER.toSort.add(posVec);

        }

        printerBlocks.clear();

        return true;
    }

    private static List<int[]> getBlocksForYLevel(WorldSchematic worldSchematic, int y) {
        if (worldSchematic == null || MC.level == null || MC.player == null) {
            return new ArrayList<>();
        }

        BlockPos.MutableBlockPos srcBlock = new BlockPos.MutableBlockPos(0, 0, 0);

        return BlockUtils.findNearBlocksByRadius(MC.player.blockPosition().mutable().setY(srcBlock.getY() + y),
                PrinterUtils.PRINTER.placeRadius.get(),
                (pos) -> {
                    srcBlock.set(pos[0], pos[1], pos[2]);

                    BlockState blockState = MC.level.getBlockState(srcBlock);

                    BlockState required = worldSchematic.getBlockState(srcBlock);

                    if (MC.player.blockPosition().closerThan(srcBlock, PrinterUtils.PRINTER.placeRadius.get())
                            && blockState.canBeReplaced()
                            && !required.isAir()
                            && blockState.getBlock() != required.getBlock()
                            && (BlockUtils.canPlace(srcBlock, MC.player.blockInteractionRange()) || (PrinterUtils.PRINTER.liquidPlace.get() && BlockUtils.canPlace(srcBlock, MC.player.blockInteractionRange(), true)))
                            && !MC.player
                            .getBoundingBox()
                            .intersects(Vec3.atLowerCornerOf(srcBlock), Vec3.atLowerCornerOf(srcBlock).add(1, 1, 1))) {

                        return (!PrinterUtils.PRINTER.strictNoColor.get()
                                && PrinterUtils.PRINTER.containedColors.contains(
                                McDataCache.getColor(required.getBlock().asItem())))
                                || (PrinterUtils.PRINTER.strictNoColor.get()
                                && PrinterUtils.PRINTER.containedBlocks.contains(required.getBlock().asItem()));
                    }

                    return false;
                }
        );
    }

    protected static List<int[]> getBlocksForYLevelBasic(WorldSchematic worldSchematic, int y) {
        if (worldSchematic == null || MC.level == null || MC.player == null) {
            return new ArrayList<>();
        }

        BlockPos.MutableBlockPos srcBlock = new BlockPos.MutableBlockPos(0, 0, 0);

        srcBlock.set(MC.player.blockPosition());

        srcBlock.setY(srcBlock.getY() + y);

        return BlockUtils.findNearBlocksByRadius(srcBlock,
                PrinterUtils.PRINTER.placeRadius.get(),
                (pos) -> {
                    srcBlock.set(pos[0], pos[1], pos[2]);

                    BlockState blockState = MC.level.getBlockState(srcBlock);

                    BlockState required = worldSchematic.getBlockState(srcBlock);

                    return MC.player.blockPosition().closerThan(srcBlock, PrinterUtils.PRINTER.placeRadius.get())
                            && blockState.canBeReplaced()
                            && !required.isAir()
                            && blockState.getBlock() != required.getBlock()
                            && (BlockUtils.canPlace(srcBlock, MC.player.blockInteractionRange()) || (PrinterUtils.PRINTER.liquidPlace.get() && BlockUtils.canPlace(srcBlock, MC.player.blockInteractionRange(), true)))
                            && !MC.player
                            .getBoundingBox()
                            .intersects(new Vec3(srcBlock), new Vec3(srcBlock).add(1, 1, 1));
                }
        );
    }

    public static void tryPlacingBlocks() {
        if (MC.player == null || MC.gameMode == null) {
            return;
        }

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null || !sortPlaceableBlocks()) {
            return;
        }

        int placed = 0;

        if (PrinterUtils.PRINTER.placeTimer < PrinterUtils.PRINTER.delay.get()) {
            return;
        }

        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        List<int[]> waterPlaceable = new ArrayList<>();

        List<PlaceableBlock> placeableBlocks = new ArrayList<>();


        for (int[] pos : PrinterUtils.PRINTER.toSort) {
            blockPos.set(pos[0], pos[1], pos[2]);

            BlockState state = worldSchematic.getBlockState(blockPos);
            Item item = state.getBlock().asItem();

            int slot = InventoryUtils.findMatchingSlot((stack, s) -> PrinterUtils.PRINTER.strictNoColor.get()
                    ? stack.getItem() == item
                    : McDataCache.getColor(stack) == McDataCache.getColor(item)
                    && PrinterUtils.PRINTER.blockExclusion.value().stream()
                    .noneMatch((block -> stack.getItem() == block.asItem())));


            if (slot == -1) {
                continue;
            }

            if (PrinterUtils.PRINTER.swapTimer > 0) {
                break;
            }

            if (!PrinterUtils.PRINTER.strictNoColor.get()
                    && McDataCache.getColor(MC.player.getMainHandItem()) != McDataCache.getColor(item)
                    || PrinterUtils.PRINTER.strictNoColor.get()
                    && MC.player.getMainHandItem().getItem() != item) {
                PrinterUtils.PRINTER.swapTimer = PrinterUtils.PRINTER.swapDelay.get();

                InventoryUtils.swapSlot(PrinterUtils.PRINTER.dedicatedSlot.get());
                InventoryUtils.swapToHotbar(slot, PrinterUtils.PRINTER.dedicatedSlot.get());
                break;
            }

            if (BlockUtils.canPlace(blockPos, MC.player.blockInteractionRange())) {
                placeableBlocks.add(new PlaceableBlock(InteractionHand.MAIN_HAND, slot, blockPos.immutable()));
                PrinterUtils.PRINTER.placeTimer = 0;
                placed++;
                PrinterUtils.PRINTER.blocksPlacedThisSec++;
            } else if (PrinterUtils.PRINTER.liquidPlace.get() && BlockUtils.canPlace(blockPos, MC.player.blockInteractionRange(), true)) {
                waterPlaceable.add(pos);
            } else {
                Psyop.log("Failed liquid place check & Air: {} - {}", blockPos.toShortString(), BlockUtils.shouldLiquidPlace(blockPos));
            }
        }

        for (PlaceableBlock placeable : placeableBlocks) {
            if (PrinterUtils.placeBlock(placeable.hand(), placeable.itemResult(), placeable.blockPos())) {
                MapArtPrinter.lastPlacedBlock.set(placeable.blockPos());
            }
        }

        for (int[] pos : waterPlaceable) {
            if (PrinterUtils.PRINTER.lastLiquidPlace > 0) {
                break;
            }

            if (PrinterUtils.PRINTER.placeTimer < PrinterUtils.PRINTER.delay.get()
                    || placed >= PrinterUtils.PRINTER.blocksPerTick.get()) {
                break;
            }

            blockPos.set(pos[0], pos[1], pos[2]);

            BlockState state = worldSchematic.getBlockState(blockPos);
            Item item = state.getBlock().asItem();

            int slot = InventoryUtils.findMatchingSlot((stack, s) -> PrinterUtils.PRINTER.strictNoColor.get()
                    ? stack.getItem() == item
                    : McDataCache.getColor(stack) == McDataCache.getColor(item)
                    && PrinterUtils.PRINTER.blockExclusion.value().stream()
                    .noneMatch((block -> stack.getItem() == block.asItem())));


            if (slot == -1) {
                continue;
            }

            InteractionHand hand = InteractionHand.MAIN_HAND;

            if (slot == 40) hand = InteractionHand.OFF_HAND;

            if (PrinterUtils.PRINTER.swapTimer > 0) {
                break;
            }

            if ((!PrinterUtils.PRINTER.strictNoColor.get()
                    && McDataCache.getColor(MC.player.getMainHandItem())
                    != McDataCache.getColor(item)
                    || (PrinterUtils.PRINTER.strictNoColor.get()
                    && MC.player.getMainHandItem().getItem() != item))
                    && hand != InteractionHand.OFF_HAND) {
                PrinterUtils.PRINTER.swapTimer = PrinterUtils.PRINTER.swapDelay.get();
                InventoryUtils.swapSlot(PrinterUtils.PRINTER.dedicatedSlot.get());
                InventoryUtils.swapToHotbar(slot, PrinterUtils.PRINTER.dedicatedSlot.get());
                break;
            }
            PrinterUtils.PRINTER.lastLiquidPlace = PrinterUtils.PRINTER.liquidPlaceTimeout.get();

            BlockPos lowerPos = blockPos.relative(Direction.DOWN);
            Psyop.log("Trying to liquid place on {}", lowerPos.toShortString());

            PacketUtils.send(new ServerboundUseItemOnPacket(hand, new BlockHitResult(BlockUtils.clickOffset(lowerPos, Direction.UP), Direction.UP, lowerPos, false), 0));
            PrinterUtils.placeBlock(hand, slot, blockPos);

            MapArtPrinter.lastPlacedBlock.set(blockPos);

            PrinterUtils.PRINTER.placeTimer = 0;
            placed++;
            PrinterUtils.PRINTER.blocksPlacedThisSec++;
        }

        waterPlaceable.clear();
    }
}
