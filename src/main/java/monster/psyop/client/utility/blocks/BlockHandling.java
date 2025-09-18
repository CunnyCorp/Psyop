package monster.psyop.client.utility.blocks;

import monster.psyop.client.utility.PacketUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;

import static monster.psyop.client.Liberty.MC;

public class BlockHandling {
    private static final BlockPos lastBlockBroken = BlockPos.ZERO;

    public static void instantBreak(BlockPos blockPos) {
        assert MC.player != null;
        if (blockPos != lastBlockBroken) {
            swing();
            PacketUtils.send(
                    new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.DOWN));
        }
        swing();
        PacketUtils.send(
                new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.DOWN));
    }

    public static void swing() {
        assert MC.player != null;
        PacketUtils.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }
}
