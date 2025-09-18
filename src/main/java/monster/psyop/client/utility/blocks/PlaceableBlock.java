package monster.psyop.client.utility.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

public record PlaceableBlock(InteractionHand hand, int itemResult, BlockPos blockPos) {
}
