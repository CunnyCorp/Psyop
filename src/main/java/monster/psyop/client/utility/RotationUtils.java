package monster.psyop.client.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static monster.psyop.client.Psyop.MC;

public class RotationUtils {
    public static final float MAGIC_N = 57.2957763671875f;

    public static float[] getRotationsTo(Vec3 targetPos) {
        if (MC.player == null) return new float[]{0, 0};

        Vec3 eyesPos = MC.player.getEyePosition();

        // Calculate differences
        double diffX = targetPos.x - eyesPos.x;
        double diffY = targetPos.y - eyesPos.y;
        double diffZ = targetPos.z - eyesPos.z;

        // Calculate horizontal distance
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        // Calculate yaw (horizontal angle)
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;

        // Calculate pitch (vertical angle)
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        // Normalize angles
        yaw = normalizeAngle(yaw);
        pitch = normalizeAngle(pitch);

        // Clamp pitch to valid range
        pitch = Mth.clamp(pitch, -90.0f, 90.0f);

        return new float[]{yaw, pitch};
    }

    /**
     * Linearly interpolates between two angles, taking the shortest path around the circle.
     *
     * @param current The current angle in degrees
     * @param target  The target angle in degrees
     * @param speed   The interpolation speed (0.0 to 1.0)
     * @return The interpolated angle
     */
    public static float lerpAngle(float current, float target, float speed) {
        // Normalize angles to 0-360 range
        current = normalizeAngle(current);
        target = normalizeAngle(target);

        // Calculate the shortest angular difference
        float difference = target - current;

        // Take the shortest path around the circle
        if (difference > 180f) {
            difference -= 360f;
        } else if (difference < -180f) {
            difference += 360f;
        }

        // Apply interpolation
        float result = current + difference * speed;

        // Normalize the result
        return normalizeAngle(result);
    }

    /**
     * Normalizes an angle to the 0-360 degree range.
     *
     * @param angle The angle to normalize
     * @return The normalized angle
     */
    public static float normalizeAngle(float angle) {
        angle %= 360f;
        if (angle < 0f) {
            angle += 360f;
        }
        return angle;
    }

    /**
     * Alternative lerp method that works with Minecraft's -180 to 180 degree system.
     *
     * @param current The current angle in degrees (-180 to 180)
     * @param target  The target angle in degrees (-180 to 180)
     * @param speed   The interpolation speed (0.0 to 1.0)
     * @return The interpolated angle in Minecraft's coordinate system
     */
    public static float lerpAngleMinecraft(float current, float target, float speed) {
        // Convert to 0-360 range for easier calculation
        float current360 = current < 0 ? current + 360 : current;
        float target360 = target < 0 ? target + 360 : target;

        // Calculate the shortest path
        float difference = target360 - current360;
        if (Math.abs(difference) > 180) {
            if (difference > 0) {
                difference -= 360;
            } else {
                difference += 360;
            }
        }

        // Apply interpolation
        float result = current + difference * speed;

        // Keep within Minecraft's -180 to 180 range
        if (result > 180f) {
            result -= 360f;
        } else if (result < -180f) {
            result += 360f;
        }

        return result;
    }

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

    public static void rotate(float pitch, float yaw) {
        assert MC.player != null : "Player was null";
        MC.player.setXRot(Mth.wrapDegrees(pitch));
        MC.player.setYRot(Mth.wrapDegrees(yaw));
    }

    public static void rotatePacket(float pitch, float yaw) {
        assert MC.player != null : "Player was null";
        MC.player.setXRot(Mth.wrapDegrees(pitch));
        MC.player.setYRot(Mth.wrapDegrees(yaw));
    }

    /**
     * Smoothly interpolates an angle toward a target, correctly handling wrap-around at ±180°.
     * speed is a lerp factor per tick in [0, 1]. Higher = faster.
     */
    public static float smoothAngle(float current, float target, float speed) {
        // Clamp speed to sane range
        float t = Mth.clamp(speed, 0.0f, 1.0f);
        return Mth.rotLerp(t, current, target);
    }

    /**
     * Smoothly rotate the player toward the given target pitch/yaw in one step (call per tick).
     * - speed: lerp factor per tick in [0, 1]
     * - clampPitch: if true, clamps pitch to [-90, 90]
     */
    public static void smoothRotateTowards(float targetYaw, float speed) {
        assert MC.player != null : "Player was null";

        float currentYaw = MC.player.getYRot();

        float nextYaw = smoothAngle(currentYaw, targetYaw, speed);

        MC.player.setYRot(nextYaw);
    }


    /**
     * Convenience: returns true if current angles are "close enough" to target (within eps).
     * Useful to decide when to stop smoothing.
     */
    public static boolean anglesReached(float currentPitch, float currentYaw, float targetPitch, float targetYaw, float epsDegrees) {
        float dPitch = Math.abs(Mth.wrapDegrees(targetPitch - currentPitch));
        float dYaw = Math.abs(Mth.wrapDegrees(targetYaw - currentYaw));
        return dPitch <= epsDegrees && dYaw <= epsDegrees;
    }

    public static void rotateTowards(float pitch, float yaw) {
        MC.player.setXRot(pitch);
        MC.player.setYRot(yaw);
    }

    /**
     * Config for mouse-acceleration emulation.
     * gain = clamp(baseGain + accel * velocity^exponent, minGain, maxGain)
     * sensitivity scales the final delta (akin to pointer sensitivity).
     *
     * @param sensitivity base scale (e.g., 0.6–1.4)
     * @param baseGain    base gain at zero velocity (e.g., 1.0)
     * @param accel       acceleration magnitude (e.g., 0.15–0.6)
     * @param exponent    response curve (e.g., 0.6–1.2)
     * @param minGain     clamp lower bound (e.g., 0.2–0.8)
     * @param maxGain     clamp upper bound (e.g., 1.5–4.0)
     * @param invertY     invert pitch
     */
    public record AccelConfig(float sensitivity, float baseGain, float accel, float exponent, float minGain,
                              float maxGain, boolean invertY) {

        public static AccelConfig defaults() {
            return new AccelConfig(1.0f, 1.0f, 0.3f, 0.9f, 0.6f, 3.0f, false);
        }
    }

    /**
     * Compute shortest signed angle difference target - current in range [-180, 180].
     */
    private static float angleDiff(float current, float target) {
        return Mth.wrapDegrees(target - current);
    }

    /**
     * Velocity-based gain curve, similar to OS pointer acceleration.
     * velocity can be angular (deg/tick) or a proxy like |delta| magnitude.
     */
    private static float accelGain(float velocity, AccelConfig cfg) {
        float v = Math.max(0f, velocity);
        float gain = cfg.baseGain + cfg.accel * (float) Math.pow(v, cfg.exponent);
        return Mth.clamp(gain, cfg.minGain, cfg.maxGain);
    }

    /**
     * Apply mouse-like acceleration to provided deltas (mouseDX, mouseDY) and write directly to player rotation.
     * dtSeconds can be used to make it time-consistent across FPS variations; pass your frame time if available.
     */
    public static void applyMouseAccelDeltas(float mouseDX, float mouseDY, float dtSeconds, AccelConfig cfg, float pitchClampMin, float pitchClampMax) {
        assert MC.player != null : "Player was null";

        // Compute "velocity" as movement rate (pixels/sec or arbitrary units).
        float speed = (dtSeconds > 0f) ? (float) (Math.hypot(mouseDX, mouseDY) / dtSeconds) : (float) Math.hypot(mouseDX, mouseDY);
        float gain = accelGain(speed, cfg);

        float yawDelta = mouseDX * cfg.sensitivity * gain;
        float pitchDelta = mouseDY * cfg.sensitivity * gain * (cfg.invertY ? 1f : -1f);

        float nextYaw = Mth.wrapDegrees(MC.player.getYRot() + yawDelta);
        float nextPitch = MC.player.getXRot() + pitchDelta;
        nextPitch = Mth.clamp(nextPitch, pitchClampMin, pitchClampMax);

        MC.player.setYRot(nextYaw);
        MC.player.setXRot(nextPitch);
    }

    /**
     * Target-based rotation with acceleration feel.
     * maxStepDegPerTick sets the base step size; acceleration scales it with current "need" (delta magnitude).
     * This creates mouse-accel-like ramps while rotating toward a target.
     */
    public static void rotateWithMouseAccelTowards(float targetPitch, float targetYaw, AccelConfig cfg, float maxStepDegPerTick, float pitchClampMin, float pitchClampMax) {
        assert MC.player != null : "Player was null";

        float curYaw = MC.player.getYRot();
        float curPitch = MC.player.getXRot();

        // Calculate shortest path differences using proper angle wrapping
        float dyaw = getShortestAngleDifference(curYaw, targetYaw);
        float dpitch = getShortestAngleDifference(curPitch, targetPitch);

        // Use angular "velocity" proxy from current need (bigger gap -> higher gain)
        float angularNeed = (float) Math.hypot(dyaw, dpitch);
        float gain = accelGain(angularNeed, cfg);

        // Base step limited by maxStepDegPerTick, scaled by gain, without overshooting
        float stepYaw = Math.copySign(Math.min(Math.abs(dyaw), maxStepDegPerTick * gain), dyaw);
        float stepPitch = Math.copySign(Math.min(Math.abs(dpitch), maxStepDegPerTick * gain), dpitch);

        // Apply sensitivity scaling last (lets sensitivity act like mouse sens)
        stepYaw *= cfg.sensitivity;
        stepPitch *= cfg.sensitivity * (cfg.invertY ? 1f : -1f);

        float nextYaw = Mth.wrapDegrees(curYaw + stepYaw);
        float nextPitch = curPitch + stepPitch;
        nextPitch = Mth.clamp(nextPitch, pitchClampMin, pitchClampMax);

        MC.player.setYRot(nextYaw);
        MC.player.setXRot(nextPitch);
    }

    /**
     * Helper method to calculate the shortest angle difference between two angles
     * Returns the difference in the range [-180, 180]
     */
    private static float getShortestAngleDifference(float current, float target) {
        float difference = target - current;
        difference = Mth.wrapDegrees(difference);
        if (difference > 180f) {
            difference -= 360f;
        } else if (difference < -180f) {
            difference += 360f;
        }
        return difference;
    }

    public static float getAngleDifference(float angle1, float angle2) {
        // Normalize both angles to be within 0-360 range
        angle1 = normalizeAngle(angle1);
        angle2 = normalizeAngle(angle2);

        // Calculate the raw difference
        float difference = angle2 - angle1;

        // Normalize the difference to be within -180 to 180 range
        if (difference > 180) {
            difference -= 360;
        } else if (difference < -180) {
            difference += 360;
        }

        return difference;
    }
}
