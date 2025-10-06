package monster.psyop.client.impl.modules.world.printer.movesets;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import static monster.psyop.client.Psyop.MC;

public class BaritoneMove extends DefaultMove {
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

    @Override
    public MoveSets type() {
        return MoveSets.BARITONE;
    }

    @Override
    public void tick(BlockPos pos) {
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(pos.relative(Direction.UP), 2));
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
