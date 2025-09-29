package monster.psyop.client.impl.modules.world.printer;

import monster.psyop.client.Psyop;
import monster.psyop.client.utility.MathUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;

import java.util.Optional;

import static monster.psyop.client.Psyop.MC;
import static monster.psyop.client.impl.modules.world.printer.PrinterUtils.PRINTER;

public class StuckFixManager {
    private static boolean runningSwimTask = false;

    public static boolean shouldCancelForSwimmingTask() {
        return MC.player != null
                && PRINTER != null
                && (PrinterUtils.shouldSwimUp() || PRINTER.wasSwimmingUp || runningSwimTask);
    }

    public static void runSwimTask() {
        if (shouldCancelForSwimmingTask()) runningSwimTask = true;
        else {
            runningSwimTask = false;
            return;
        }

        //PrinterUtils.EXECUTOR.execute(
        //        () -> {
        if (MC.player == null) return;

        if (PrinterUtils.shouldSwimUp()) {
            PRINTER.wasSwimmingUp = true;
            MC.player.setSwimming(false);
            MC.player.stopFallFlying();

            if (MC.player.isInLiquid()) {
                if (PRINTER.airOpeningTemp == null) {
                    Optional<BlockPos> potentialOpening =
                            PrinterUtils.findAirOpening(MC.player.blockPosition(), PRINTER.o2Radius.get());

                    if (potentialOpening.isEmpty()) {
                        return;
                    }

                    PRINTER.airOpeningTemp = potentialOpening.get();
                } else {
                    if (!BlockUtils.isReplaceable(PRINTER.airOpeningTemp)) {
                        PRINTER.airOpeningTemp = null;
                        return;
                    }

                    if (MC.player.getEyeY() < PRINTER.airOpeningTemp.getY()) {
                        if (MathUtils.xzDistanceBetween(MC.player.blockPosition(), PRINTER.airOpeningTemp)
                                > 0.5) {
                            MC.player.setXRot(RotationUtils.getPitch(PRINTER.airOpeningTemp));
                            MC.player.setYRot(RotationUtils.getYaw(PRINTER.airOpeningTemp));

                            if (!MC.options.keyUp.isDown()) MC.options.keyUp.setDown(true);

                            Psyop.log(
                                    "Try to swim up to {}", PRINTER.airOpeningTemp.toShortString());

                            MC.options.keyJump.setDown(MC.player.getEyeY() < PRINTER.airOpeningTemp.getY() - 2.0);
                        } else {
                            MC.options.keyUp.setDown(false);
                            if (!MC.options.keyJump.isDown()) MC.options.keyJump.setDown(true);
                        }
                        return;
                    }
                }
            }

            if (PRINTER.savingGrace == null && PRINTER.savingGraceTimer < 0) {
                PRINTER.savingGraceTimer = PRINTER.savingGraceDelay.get();
                Optional<BlockPos> suitablePos =
                        PrinterUtils.findClosestSuitableLand(
                                PRINTER.airOpeningTemp != null
                                        ? PRINTER.airOpeningTemp
                                        : new BlockPos(MC.player.getBlockX(), 63, MC.player.getBlockZ()),
                                PRINTER.savingGraceRadius.get());

                suitablePos.ifPresent(pos -> PRINTER.savingGrace = pos);
            } else if (PRINTER.savingGrace != null) {
                MC.player.setYRot(RotationUtils.getYaw(PRINTER.savingGrace));
                if (!MC.options.keyUp.isDown()) MC.options.keyUp.setDown(true);
                if (!MC.options.keyJump.isDown()) MC.options.keyJump.setDown(true);

                if (BlockUtils.isReplaceable(PRINTER.savingGrace)
                        || BlockUtils.isNotAir(PRINTER.savingGrace.above())
                        || BlockUtils.isNotAir(PRINTER.savingGrace.above(2))) {
                    PRINTER.savingGrace = null;
                    PRINTER.savingGraceTimer = -1;
                    return;
                }
            }

            PRINTER.savingGraceTimer--;
        } else if (PRINTER.wasSwimmingUp) {
            if (MC.player.onGround() || !MC.player.isInLiquid()) {
                MC.options.keyJump.setDown(false);
                MC.options.keyUp.setDown(false);
                PRINTER.wasSwimmingUp = false;
                PRINTER.savingGrace = null;
            } else {
                if (PRINTER.savingGrace != null) {
                    MC.player.setYRot(RotationUtils.getYaw(PRINTER.savingGrace));
                    if (!MC.options.keyUp.isDown()) MC.options.keyUp.setDown(true);
                    if (!MC.options.keyJump.isDown()) MC.options.keyJump.setDown(true);
                }
            }
        }

        runningSwimTask = false;
        //});
    }
}
