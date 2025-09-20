package monster.psyop.client.impl.modules.world.printer.movesets;

import monster.psyop.client.impl.modules.world.printer.PrinterUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import static monster.psyop.client.Psyop.MC;

public class AdvancedMove extends DefaultMove {
    private int holdBackTicks = 0;

    @Override
    public MoveSets type() {
        return MoveSets.ADVANCED;
    }

    @Override
    public void tick(BlockPos pos) {
        assert MC.player != null;
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
            if (MC.player.onGround()) {
                int xOffset = MC.player.getMotionDirection().getStepX();
                int zOffset = MC.player.getMotionDirection().getStepZ();
                int xOffsetOpp = MC.player.getMotionDirection().getOpposite().getStepX();
                int zOffsetOpp = MC.player.getMotionDirection().getOpposite().getStepZ();

                BlockPos.MutableBlockPos fwBlock = MC.player.getOnPos().mutable();

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

                BlockPos.MutableBlockPos backBlock = MC.player.blockPosition().mutable();

                backBlock.setX(backBlock.getX() + xOffsetOpp);
                backBlock.setZ(backBlock.getZ() + zOffsetOpp);

                BlockPos.MutableBlockPos ffBlock = fwBlock.mutable();

                ffBlock.setX(ffBlock.getX() + xOffset);
                ffBlock.setY(PrinterUtils.PRINTER.yLevel.get());
                ffBlock.setX(ffBlock.getZ() + zOffset);

                assert MC.level != null;
                BlockState fwBlockState = MC.level.getBlockState(fwBlock);

                if (fwBlockState.getInteractionShape(MC.level, fwBlock).max(Direction.Axis.Y) == 0.5) {
                    yaw = Mth.wrapDegrees((float) (yaw + (RotationUtils.getYaw(fwBlock.getBottomCenter()) * 0.3)));
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

        MC.options.keyUp.setDown(moveForward);
        MC.options.keyDown.setDown(moveBack);
        MC.player.setSprinting(sprinting);
        MC.options.keyJump.setDown(jump);
        MC.player.setYRot(yaw);
        MC.player.setXRot(pitch);
    }

    @Override
    public void cancel(BlockPos pos) {
        if (MC.player == null) {
            return;
        }

        MC.options.keyUp.setDown(false);
        MC.options.keyDown.setDown(false);
        MC.options.keyLeft.setDown(false);
        MC.options.keyRight.setDown(false);
    }
}
