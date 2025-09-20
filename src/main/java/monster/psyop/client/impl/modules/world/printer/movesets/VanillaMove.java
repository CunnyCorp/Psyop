package monster.psyop.client.impl.modules.world.printer.movesets;

import monster.psyop.client.utility.RotationUtils;
import net.minecraft.core.BlockPos;

import static monster.psyop.client.Psyop.MC;


public class VanillaMove extends DefaultMove {
    @Override
    public MoveSets type() {
        return MoveSets.VANILLA;
    }

    @Override
    public void tick(BlockPos pos) {
        assert MC.player != null;

        MC.player.setYRot((float) RotationUtils.getYaw(pos));

        MC.options.keyDown.setDown(false);
        MC.options.keyLeft.setDown(false);
        MC.options.keyRight.setDown(false);

        if (!MC.options.keyUp.isDown()) {
            MC.options.keyUp.setDown(true);
        }
    }

    @Override
    public void cancel(BlockPos pos) {
        if (MC.player == null) {
            return;
        }

        if (MC.options.keyUp.isDown()) {
            MC.options.keyUp.setDown(false);
        }
    }
}
