package monster.psyop.client.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class MathUtils {
    public static double toMapQuad(double v) {
        int j = (int) Math.floor((v + 64.0) / (double) 128);
        int l = j * 128 + 128 / 2 - 64;
        return (double) l / 128;
    }

    public static double xzDistanceBetween(Vec3 s, Vec3 e) {
        var dx = s.x - e.x;
        var dz = s.z - e.z;
        return dx * dx + dz * dz;
    }

    public static double xzDistanceBetween(Vec3 s, BlockPos e) {
        var dx = s.x - e.getX();
        var dz = s.z - e.getZ();
        return dx * dx + dz * dz;
    }

    public static double xzDistanceBetween(double x, double z, double x1, double z1) {
        var dx = x - x1;
        var dz = z - z1;
        return dx * dx + dz * dz;
    }

    public static double xzDistanceBetween(BlockPos s, BlockPos e) {
        var dx = s.getX() - e.getX();
        var dz = s.getZ() - e.getZ();
        return dx * dx + dz * dz;
    }

    public static double xyzDistanceBetween(
            double x, double y, double z, double x1, double y1, double z1) {
        var dx = x - x1;
        var dy = y - y1;
        var dz = z - z1;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double xyzDistanceBetween(BlockPos s, BlockPos e) {
        var dx = s.getX() - e.getX();
        var dy = s.getY() - e.getY();
        var dz = s.getZ() - e.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public static double xyzDistanceBetween(Vec3 s, BlockPos e) {
        var dx = s.x() - e.getX();
        var dy = s.y() - e.getY();
        var dz = s.z() - e.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public static Vec3 set(Vec3 vec3, double x, double y, double z) {
        vec3.x = x;
        vec3.y = y;
        vec3.z = z;

        return vec3;
    }

    public static Vec3 setX(Vec3 vec3, double x) {
        vec3.x = x;

        return vec3;
    }

    public static Vec3 setY(Vec3 vec3, double y) {
        vec3.y = y;

        return vec3;
    }

    public static Vec3 setZ(Vec3 vec3, double z) {
        vec3.z = z;

        return vec3;
    }
}
