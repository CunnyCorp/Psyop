package monster.psyop.client.impl.modules.world.printer;

import monster.psyop.client.Liberty;
import monster.psyop.client.utility.MathUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;

import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static monster.psyop.client.impl.modules.world.printer.PrinterUtils.PRINTER;

public class StuckFixManager {
    private static boolean runningSwimTask = false;

    public static boolean shouldCancelForSwimmingTask() {
        return mc.player != null
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
        if (mc.player == null) return;

        if (PrinterUtils.shouldSwimUp()) {
            PRINTER.wasSwimmingUp = true;
            mc.player.setSwimming(false);
            mc.player.stopFallFlying();

            if (mc.player.isInLiquid()) {
                if (PRINTER.airOpeningTemp == null) {
                    Optional<BlockPos> potentialOpening =
                            PrinterUtils.findAirOpening(mc.player.blockPosition(), PRINTER.o2Radius.get());

                    if (potentialOpening.isEmpty()) {
                        return;
                    }

                    PRINTER.airOpeningTemp = potentialOpening.get();
                } else {
                    if (!BlockUtils.isReplaceable(PRINTER.airOpeningTemp)) {
                        PRINTER.airOpeningTemp = null;
                        return;
                    }

                    if (mc.player.getEyeY() < PRINTER.airOpeningTemp.getY()) {
                        if (MathUtils.xzDistanceBetween(mc.player.blockPosition(), PRINTER.airOpeningTemp)
                                > 0.5) {
                            mc.player.setXRot(RotationUtils.getPitch(PRINTER.airOpeningTemp));
                            mc.player.setYRot(RotationUtils.getYaw(PRINTER.airOpeningTemp));

                            if (!mc.options.keyUp.isDown()) mc.options.keyUp.setDown(true);

                            Liberty.log(
                                    "Try to swim up to {}", PRINTER.airOpeningTemp.toShortString());

                            mc.options.keyJump.setDown(mc.player.getEyeY() < PRINTER.airOpeningTemp.getY() - 2.0);
                        } else {
                            mc.options.keyUp.setDown(false);
                            if (!mc.options.keyJump.isDown()) mc.options.keyJump.setDown(true);
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
                                        : new BlockPos(mc.player.getBlockX(), 63, mc.player.getBlockZ()),
                                PRINTER.savingGraceRadius.get());

                suitablePos.ifPresent(pos -> PRINTER.savingGrace = pos);
            } else if (PRINTER.savingGrace != null) {
                mc.player.setYRot(RotationUtils.getYaw(PRINTER.savingGrace));
                if (!mc.options.keyUp.isDown()) mc.options.keyUp.setDown(true);
                if (!mc.options.keyJump.isDown()) mc.options.keyJump.setDown(true);

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
            if (mc.player.onGround()) {
                mc.options.keyJump.setDown(false);
                mc.options.keyUp.setDown(false);
                PRINTER.wasSwimmingUp = false;
                PRINTER.savingGrace = null;
            } else {
                if (PRINTER.savingGrace != null) {
                    mc.player.setYRot(RotationUtils.getYaw(PRINTER.savingGrace));
                    if (!mc.options.keyUp.isDown()) mc.options.keyUp.setDown(true);
                    if (!mc.options.keyJump.isDown()) mc.options.keyJump.setDown(true);
                }
            }
        }

        runningSwimTask = false;
        //});
    }
}
