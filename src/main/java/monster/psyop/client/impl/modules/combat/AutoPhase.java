package monster.psyop.client.impl.modules.combat;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
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
import net.minecraft.world.level.block.state.BlockState;

public class AutoPhase extends Module {
    private final BoolSetting botMode = new BoolSetting.Builder()
            .name("bot-mode")
            .defaultTo(true)
            .addTo(coreGroup);
    public final FloatSetting minPitch = new FloatSetting.Builder()
            .name("min-pitch")
            .defaultTo(80f)
            .range(60f, 89f)
            .addTo(coreGroup);

    public int phaseTime = -1;
    public BlockPos phasePos = null;
    public float yaw = 0f;
    public float pitch = 0f;
    public int pressW = -1;

    public AutoPhase() {
        super(Categories.COMBAT, "auto-phase", "Keeps you phased with pearls!");
    }

    @Override
    public void update() {
        phaseTime--;

        if (pressW > 0) {
            MC.options.keyUp.setDown(true);
            MC.player.setYRot(yaw);
            pressW--;
        }

        if (pressW == 0) {
            MC.options.keyUp.setDown(false);
            MC.player.setYRot(yaw);
            pressW = -1;
        }

        if (phaseTime == 2) {
            int slot = MiddleClickUse.findItemInHotbar(Items.ENDER_PEARL);

            if (slot == -1) {
                phaseTime = -1;
                return;
            }

            PacketUtils.send(new ServerboundSetCarriedItemPacket(slot));
            MC.player.getInventory().setSelectedSlot(slot);

            yaw = getPhaseYaw();
            pitch = getPhasePitch();

            PacketUtils.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, yaw, pitch));

            if (botMode.get()) {
                pressW = 2;
            }

            return;
        }

        if (phaseTime <= -1 && BlockUtils.isAir(MC.player.blockPosition())) {
            phasePos = getPhasePos();

            if (phasePos != null) {
                phaseTime = 3;
            }
        }
    }

    public BlockPos getPhasePos() {
        BlockPos original = MC.player.blockPosition();

        BlockPos cornerBlock = checkCorners(original);

        if (cornerBlock != null) {
            return cornerBlock;
        }

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (Direction dir : BlockUtils.HORIZONTALS) {
            mutableBlockPos.set(original.getX() + dir.getStepX(), original.getY(), original.getZ() + dir.getStepZ());

            if (MC.level.getBlockState(mutableBlockPos).getBlock().defaultDestroyTime() >= 50) {
                return mutableBlockPos;
            }
        }

        return null;
    }

    public BlockPos checkCorners(BlockPos original) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (Direction dir1 : BlockUtils.HORIZONTALS) {
            Direction dir2 = dir1.getClockWise();

            mutableBlockPos.set(original.getX() + dir1.getStepX() + dir2.getStepX(),
                    original.getY(),
                    original.getZ() + dir1.getStepZ() + dir2.getStepZ());

            BlockState cornerBlockState = MC.level.getBlockState(mutableBlockPos);

            if (cornerBlockState.getBlock().getExplosionResistance() >= 50) {
                BlockPos adjacent1 = original.relative(dir1);
                BlockState adjacent1State = MC.level.getBlockState(adjacent1);

                BlockPos adjacent2 = original.relative(dir2);
                BlockState adjacent2State = MC.level.getBlockState(adjacent2);

                if (adjacent1State.getBlock().getExplosionResistance() >= 50 &&
                        adjacent2State.getBlock().getExplosionResistance() >= 50) {
                    return mutableBlockPos.immutable();
                }
            }
        }

        return null;
    }

    public float getPhaseYaw() {
        return RotationUtils.getYaw(phasePos.getCenter());
    }

    public float getPhasePitch() {
        return Math.max(minPitch.get(), RotationUtils.getPitch(phasePos.getBottomCenter()));
    }
}
