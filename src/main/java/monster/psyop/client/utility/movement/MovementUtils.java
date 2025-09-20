package monster.psyop.client.utility.movement;

import monster.psyop.client.Psyop;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class MovementUtils {
    private static final double MOVEMENT_THRESHOLD = 0.1;

    /**
     * Moves the player towards a specific direction using only input controls
     *
     * @param xDirection X component of movement direction (-1 to 1)
     * @param zDirection Z component of movement direction (-1 to 1)
     * @param intensity  Movement intensity (0 to 1)
     */
    public static void moveTowards(double xDirection, double zDirection, float intensity) {
        if (Psyop.MC.player == null) return;

        // Normalize the direction vector
        double length = Math.sqrt(xDirection * xDirection + zDirection * zDirection);
        if (length < MOVEMENT_THRESHOLD) {
            resetMovement();
            return;
        }

        double normalizedX = xDirection / length;
        double normalizedZ = zDirection / length;

        // Apply intensity
        normalizedX *= intensity;
        normalizedZ *= intensity;

        // Determine which movement keys to press based on direction
        handleDirectionalInputs(normalizedX, normalizedZ);
    }

    /**
     * Moves the player towards a specific world coordinate using only input controls
     *
     * @param targetX   Target X coordinate
     * @param targetZ   Target Z coordinate
     * @param intensity Movement intensity (0 to 1)
     */
    public static void moveTowardsSec(double targetX, double targetZ, float intensity) {
        if (Psyop.MC.player == null) return;

        double deltaX = targetX - Psyop.MC.player.getX();
        double deltaZ = targetZ - Psyop.MC.player.getZ();

        moveTowards(deltaX, deltaZ, intensity);
    }

    /**
     * Moves the player towards a specific block position using only input controls
     *
     * @param targetPos Target block position
     * @param intensity Movement intensity (0 to 1)
     */
    public static void moveTowards(net.minecraft.core.BlockPos targetPos, float intensity) {
        moveTowards(targetPos.getX() + 0.5, targetPos.getZ() + 0.5, intensity);
    }

    /**
     * Handles directional movement inputs based on the desired movement vector
     *
     * @param xMovement Desired X movement (-1 to 1)
     * @param zMovement Desired Z movement (-1 to 1)
     */
    private static void handleDirectionalInputs(double xMovement, double zMovement) {
        // Get player's look direction for relative movement
        float yaw = Psyop.MC.player.getYRot();
        double radYaw = Math.toRadians(yaw);

        // Rotate movement vector to be relative to player's view
        double rotatedX = xMovement * Math.cos(-radYaw) - zMovement * Math.sin(-radYaw);
        double rotatedZ = xMovement * Math.sin(-radYaw) + zMovement * Math.cos(-radYaw);

        // Determine which keys to press based on the rotated movement vector
        handleCardinalMovement(rotatedX, rotatedZ);
    }

    /**
     * Handles cardinal direction movement based on relative movement vector
     *
     * @param relativeX Relative X movement (-1 to 1)
     * @param relativeZ Relative Z movement (-1 to 1)
     */
    private static void handleCardinalMovement(double relativeX, double relativeZ) {
        // Reset all movement keys first
        resetMovement();

        // Determine dominant movement direction with deadzone
        double absX = Math.abs(relativeX);
        double absZ = Math.abs(relativeZ);

        if (absX < MOVEMENT_THRESHOLD && absZ < MOVEMENT_THRESHOLD) {
            return; // No significant movement
        }

        // Handle forward/backward movement
        if (absZ > absX) {
            if (relativeZ > MOVEMENT_THRESHOLD) {
                Psyop.MC.options.keyUp.setDown(true);
            } else if (relativeZ < -MOVEMENT_THRESHOLD) {
                Psyop.MC.options.keyDown.setDown(true);
            }
        }
        // Handle left/right movement
        else {
            if (relativeX > MOVEMENT_THRESHOLD) {
                Psyop.MC.options.keyRight.setDown(true);
            } else if (relativeX < -MOVEMENT_THRESHOLD) {
                Psyop.MC.options.keyLeft.setDown(true);
            }
        }

        // Handle diagonal movement (both axes significant)
        if (absX > MOVEMENT_THRESHOLD * 0.7 && absZ > MOVEMENT_THRESHOLD * 0.7) {
            if (relativeZ > MOVEMENT_THRESHOLD) {
                Psyop.MC.options.keyUp.setDown(true);
            } else if (relativeZ < -MOVEMENT_THRESHOLD) {
                Psyop.MC.options.keyDown.setDown(true);
            }

            if (relativeX > MOVEMENT_THRESHOLD) {
                Psyop.MC.options.keyRight.setDown(true);
            } else if (relativeX < -MOVEMENT_THRESHOLD) {
                Psyop.MC.options.keyLeft.setDown(true);
            }
        }
    }

    /**
     * Resets all movement keys to not pressed
     */
    public static void resetMovement() {
        Psyop.MC.options.keyUp.setDown(false);
        Psyop.MC.options.keyDown.setDown(false);
        Psyop.MC.options.keyLeft.setDown(false);
        Psyop.MC.options.keyRight.setDown(false);
    }

    /**
     * Strafes left using only input controls
     *
     * @param intensity Strafing intensity (0 to 1)
     */
    public static void strafeLeft(float intensity) {
        resetMovement();
        if (intensity > MOVEMENT_THRESHOLD) {
            Psyop.MC.options.keyLeft.setDown(true);
        }
    }

    /**
     * Strafes right using only input controls
     *
     * @param intensity Strafing intensity (0 to 1)
     */
    public static void strafeRight(float intensity) {
        resetMovement();
        if (intensity > MOVEMENT_THRESHOLD) {
            Psyop.MC.options.keyRight.setDown(true);
        }
    }

    /**
     * Moves forward using only input controls
     *
     * @param intensity Movement intensity (0 to 1)
     */
    public static void moveForward(float intensity) {
        resetMovement();
        if (intensity > MOVEMENT_THRESHOLD) {
            Psyop.MC.options.keyUp.setDown(true);
        }
    }

    /**
     * Moves backward using only input controls
     *
     * @param intensity Movement intensity (0 to 1)
     */
    public static void moveBackward(float intensity) {
        resetMovement();
        if (intensity > MOVEMENT_THRESHOLD) {
            Psyop.MC.options.keyDown.setDown(true);
        }
    }

    /**
     * Gets the movement direction as a normalized vector
     *
     * @return Normalized movement direction vector
     */
    public static Vec3 getMovementDirection() {
        double x = 0;
        double z = 0;

        if (Psyop.MC.options.keyUp.isDown()) z += 1;
        if (Psyop.MC.options.keyDown.isDown()) z -= 1;
        if (Psyop.MC.options.keyLeft.isDown()) x -= 1;
        if (Psyop.MC.options.keyRight.isDown()) x += 1;

        // Normalize if moving diagonally
        double length = Math.sqrt(x * x + z * z);
        if (length > MOVEMENT_THRESHOLD) {
            x /= length;
            z /= length;
        }

        return new Vec3(x, 0, z);
    }

    /**
     * Checks if the player is currently moving
     *
     * @return True if any movement key is pressed
     */
    public static boolean isMoving() {
        return Psyop.MC.options.keyUp.isDown() ||
                Psyop.MC.options.keyDown.isDown() ||
                Psyop.MC.options.keyLeft.isDown() ||
                Psyop.MC.options.keyRight.isDown();
    }

    /**
     * Gets the facing direction of the player
     *
     * @return The cardinal direction the player is facing
     */
    public static Direction getFacingDirection() {
        float yaw = Psyop.MC.player.getYRot();
        yaw = (yaw % 360 + 360) % 360; // Normalize to 0-360

        if (yaw >= 315 || yaw < 45) return Direction.SOUTH;
        if (yaw >= 45 && yaw < 135) return Direction.WEST;
        if (yaw >= 135 && yaw < 225) return Direction.NORTH;
        return Direction.EAST;
    }
}