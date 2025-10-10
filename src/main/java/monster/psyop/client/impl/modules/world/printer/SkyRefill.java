package monster.psyop.client.impl.modules.world.printer;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.ItemListSetting;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class SkyRefill extends Module {
    public ItemListSetting validBlocks =
            new ItemListSetting.Builder()
                    .name("valid-blocks")
                    .defaultTo(List.of(Items.NETHERITE_BLOCK))
                    .addTo(coreGroup);
    public ItemListSetting refillBlocks =
            new ItemListSetting.Builder()
                    .name("refill-blocks")
                    .defaultTo(List.of(Items.CYAN_SHULKER_BOX))
                    .filter((v) -> v.getDescriptionId().contains("shulker"))
                    .addTo(coreGroup);

    private int emptyTimer = 0;
    private int idle = 0;
    private boolean useUnder = false;

    public SkyRefill() {
        super(Categories.WORLD, "sky-refill", "Auto refill from inventory for sky arts");
    }

    @Override
    public boolean inUse() {
        return emptyTimer > 0;
    }

    @Override
    public boolean controlsHotbar() {
        return true;
    }

    @Override
    public void update() {
        int validSlot = InventoryUtils.findAnySlot(validBlocks.value());
        System.out.println(validSlot + "-" + emptyTimer);
        if (validSlot == -1) {
            emptyTimer++;
        } else {
            emptyTimer = 0;
            return;
        }

        if (idle > 0) {
            idle--;
            return;
        }

        if (useUnder) {
            BlockPos blockPos = MC.player.blockPosition().relative(Direction.DOWN, 2);
            MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, new BlockHitResult(BlockUtils.clickOffset(blockPos, Direction.DOWN), Direction.DOWN, blockPos, false));
            useUnder = false;
        }

        if (emptyTimer > 60) {
            if (!refillBlocks.value().contains(MC.player.getMainHandItem().getItem())) {
                InventoryUtils.swapSlot(4);

                int slot = InventoryUtils.findAnySlot(refillBlocks.value());

                if (slot == -1) {
                    return;
                }

                InventoryUtils.swapToHotbar(slot, 4);
                idle = 40;
            } else {
                BlockPos blockPos = MC.player.blockPosition().relative(Direction.DOWN, 1);
                MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, new BlockHitResult(BlockUtils.clickOffset(blockPos, Direction.DOWN), Direction.DOWN, blockPos, false));
                useUnder = true;
                idle = 40;
                emptyTimer = 10;
            }
        }
    }
}
