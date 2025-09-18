package monster.psyop.client.impl.modules.world.printer.movesets;

import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.core.BlockPos;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class VanillaMove extends DefaultMove {
    @Override
    public MoveSets type() {
        return MoveSets.VANILLA;
    }

    @Override
    public void tick(BlockPos pos) {
        assert mc.player != null;

        mc.player.setYRot((float) Rotations.getYaw(pos));

        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);

        if (!mc.options.keyUp.isDown()) {
            mc.options.keyUp.setDown(true);
        }
    }

    @Override
    public void cancel(BlockPos pos) {
        if (mc.player == null) {
            return;
        }

        if (mc.options.keyUp.isDown()) {
            mc.options.keyUp.setDown(false);
        }
    }
}
