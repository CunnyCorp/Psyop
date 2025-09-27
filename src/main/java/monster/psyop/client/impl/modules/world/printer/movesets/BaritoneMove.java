package monster.psyop.client.impl.modules.world.printer.movesets;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalNear;
import monster.psyop.client.impl.modules.world.printer.PrinterUtils;
import net.minecraft.core.BlockPos;

import static monster.psyop.client.Psyop.MC;

public class BaritoneMove extends DefaultMove {
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    private int timer = 0;

    @Override
    public MoveSets type() {
        return MoveSets.BARITONE;
    }

    @Override
    public void tick(BlockPos pos) {
        if (timer++ >= PrinterUtils.PRINTER.processWait.get()) {
            System.out.println("Baritone Move");
            baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(pos, this.radius()));
            timer = 0;
        }
    }

    @Override
    public void cancel(BlockPos pos) {
        if (MC.player == null) {
            return;
        }

        if (baritone.getPathingBehavior().hasPath()) {
            baritone.getPathingBehavior().cancelEverything();
        }
    }
}
