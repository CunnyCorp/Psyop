package monster.psyop.client.impl.modules.world.printer.movesets;

import net.minecraft.core.BlockPos;

public abstract class DefaultMove {
    private int radius = 1;

    public int radius() {
        return radius;
    }

    public void radius(int i) {
        radius = i;
    }

    public abstract MoveSets type();

    public abstract void tick(BlockPos pos);

    public abstract void cancel(BlockPos pos);
}
