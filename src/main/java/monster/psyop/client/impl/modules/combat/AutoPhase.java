package monster.psyop.client.impl.modules.combat;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.modules.player.MiddleClickUse;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

public class AutoPhase extends Module {
    public int phaseTime = -1;
    public BlockPos phasePos = null;

    public AutoPhase() {
        super(Categories.COMBAT, "auto-phase", "Keeps you phased with pearls!");
    }

    @Override
    public void update() {
        phaseTime--;

        if (phaseTime == 2) {
            int slot = MiddleClickUse.findItemInHotbar(Items.ENDER_PEARL);

            if (slot == -1) {
                phaseTime = -1;
                return;
            }

            PacketUtils.send(new ServerboundSetCarriedItemPacket(slot));
            PacketUtils.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, getPhaseYaw(), getPhasePitch()));

            return;
        }

        if (phaseTime == -1 && BlockUtils.isAir(MC.player.blockPosition())) {
            BlockPos original = MC.player.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Direction dir : BlockUtils.HORIZONTALS) {
                mutableBlockPos.set(original.getX() + dir.getStepX(), original.getY(), original.getZ() + dir.getStepZ());

                if (MC.level.getBlockState(mutableBlockPos).getBlock().defaultDestroyTime() >= 50) {
                    phaseTime = 3;
                    phasePos = mutableBlockPos.immutable();
                }
            }
        }
    }

    public float getPhaseYaw() {
        return RotationUtils.getYaw(phasePos.getCenter());
    }

    public float getPhasePitch() {
        return RotationUtils.getPitch(phasePos.getBottomCenter().add(0.0f, 0.3f, 0.0f));
    }
}
