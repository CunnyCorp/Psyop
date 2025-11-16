package monster.psyop.client.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static monster.psyop.client.Psyop.MC;

public class RotationUtils {
    public static final float MAGIC_N = 57.2957763671875f;

    public static float getPitch(BlockPos pos) {
        return getPitch(pos.getCenter());
    }

    public static float getYaw(BlockPos pos) {
        return getYaw(pos.getCenter());
    }

    public static float getPitch(Vec3 pos) {
        assert MC.player != null : "Player was null";
        double x = pos.x() - MC.player.getX();
        double y = pos.y() - MC.player.getEyeY();
        double z = pos.z() - MC.player.getZ();
        double xzSqr = Math.sqrt(x * x + z * z);

        return (float) Mth.wrapDegrees(-(Mth.atan2(y, xzSqr) * MAGIC_N));
    }

    public static float getYaw(Vec3 pos) {
        assert MC.player != null : "Player was null";
        double x = pos.x() - MC.player.getX();
        double z = pos.z() - MC.player.getZ();
        return (float) Mth.wrapDegrees((Mth.atan2(z, x) * MAGIC_N) - 90.0f);
    }
}
