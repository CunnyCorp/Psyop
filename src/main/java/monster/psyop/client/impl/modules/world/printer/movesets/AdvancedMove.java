package monster.psyop.client.impl.modules.world.printer.movesets;

import meteordevelopment.meteorclient.utils.player.Rotations;
import monster.psyop.client.impl.modules.world.printer.PrinterUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AdvancedMove extends DefaultMove {
    private int holdBackTicks = 0;

    @Override
    public MoveSets type() {
        return MoveSets.ADVANCED;
    }

    @Override
    public void tick(BlockPos pos) {
        assert mc.player != null;
        boolean moveForward = true;
        boolean moveBack = false;
        boolean jump = false;
        boolean sprinting = false;
        boolean holdFurtherLogic = false;
        float yaw = RotationUtils.getYaw(pos);
        float pitch = RotationUtils.getPitch(pos);

        if (PrinterUtils.PRINTER.alwaysSprint.get()) {
            sprinting = true;
        }

        if (holdBackTicks > 0) {
            holdBackTicks--;
            moveBack = true;
            moveForward = false;
            holdFurtherLogic = true;
        }

        if (!holdFurtherLogic) {
            if (mc.player.onGround()) {
                int xOffset = mc.player.getMotionDirection().getStepX();
                int zOffset = mc.player.getMotionDirection().getStepZ();
                int xOffsetOpp = mc.player.getMotionDirection().getOpposite().getStepX();
                int zOffsetOpp = mc.player.getMotionDirection().getOpposite().getStepZ();

                BlockPos.MutableBlockPos fwBlock = mc.player.getOnPos().mutable();

                boolean wouldNotBeSafe = false;

                for (int x0 = 0; x0 <= 1; x0++) {
                    if (xOffset != 0) {
                        fwBlock.setX(fwBlock.getX() + (xOffset * x0));
                    } else {
                        fwBlock.setX(fwBlock.getX());
                    }
                    for (int z0 = 0; z0 <= 1; z0++) {
                        if (zOffset != 0) {
                            fwBlock.setZ(fwBlock.getZ() + (zOffset * z0));
                        } else {
                            fwBlock.setZ(fwBlock.getZ());
                        }

                        if (BlockUtils.isReplaceable(fwBlock)) {
                            wouldNotBeSafe = true;
                            break;
                        }
                    }
                }

                fwBlock.setX(fwBlock.getX() + xOffset);
                fwBlock.setZ(fwBlock.getZ() + zOffset);

                BlockPos.MutableBlockPos backBlock = mc.player.blockPosition().mutable();

                backBlock.setX(backBlock.getX() + xOffsetOpp);
                backBlock.setZ(backBlock.getZ() + zOffsetOpp);

                BlockPos.MutableBlockPos ffBlock = fwBlock.mutable();

                ffBlock.setX(ffBlock.getX() + xOffset);
                ffBlock.setY(PrinterUtils.PRINTER.yLevel.get());
                ffBlock.setX(ffBlock.getZ() + zOffset);

                assert mc.level != null;
                BlockState fwBlockState = mc.level.getBlockState(fwBlock);

                if (fwBlockState.getInteractionShape(mc.level, fwBlock).max(Direction.Axis.Y) == 0.5) {
                    yaw = Mth.wrapDegrees((float) (yaw + (Rotations.getYaw(fwBlock.getBottomCenter()) * 0.3)));
                } else {
                    if (wouldNotBeSafe) {
                        fwBlock.setY(PrinterUtils.PRINTER.yLevel.get());
                        if (PrinterUtils.PRINTER.safeWalk.get()) {
                            moveBack = true;
                            moveForward = false;
                            holdBackTicks = PrinterUtils.PRINTER.backHoldTime.get();
                        }
                    }
                }
            }
        }

        mc.options.keyUp.setDown(moveForward);
        mc.options.keyDown.setDown(moveBack);
        mc.player.setSprinting(sprinting);
        mc.options.keyJump.setDown(jump);
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }

    @Override
    public void cancel(BlockPos pos) {
        if (mc.player == null) {
            return;
        }

        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
    }
}
