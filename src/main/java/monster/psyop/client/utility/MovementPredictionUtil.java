package monster.psyop.client.utility;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MovementPredictionUtil {

    /**
     * Predicts future positions of an entity based on its current velocity
     *
     * @param entity     The entity to predict
     * @param steps      Number of future positions to predict
     * @param multiplier Velocity multiplier for prediction
     * @return List of predicted future positions
     */
    public static List<Vec3> predictFuturePositions(Entity entity, int steps, double multiplier) {
        return predictFuturePositions(entity, steps, multiplier, 0.1);
    }

    /**
     * Predicts future positions of an entity based on its current velocity
     *
     * @param entity      The entity to predict
     * @param steps       Number of future positions to predict
     * @param multiplier  Velocity multiplier for prediction
     * @param minVelocity Minimum velocity threshold (squared)
     * @return List of predicted future positions
     */
    public static List<Vec3> predictFuturePositions(Entity entity, int steps, double multiplier, double minVelocity) {
        List<Vec3> predictions = new ArrayList<>();

        if (entity == null || steps <= 0) {
            return predictions;
        }

        Vec3 currentPos = entity.position();
        Vec3 velocity = entity.getDeltaMovement().multiply(multiplier, multiplier, multiplier);

        // Check if velocity is above minimum threshold
        if (velocity.lengthSqr() < minVelocity * minVelocity) {
            return predictions;
        }

        for (int i = 1; i <= steps; i++) {
            Vec3 predictedPos = currentPos.add(velocity.multiply(i, i, i));
            predictions.add(predictedPos);
        }

        return predictions;
    }

    /**
     * Predicts future positions using historical data for more accurate prediction
     *
     * @param entity           The entity to predict
     * @param currentPosition  Current position
     * @param previousPosition Previous position (from previous tick)
     * @param steps            Number of future positions to predict
     * @param multiplier       Velocity multiplier for prediction
     * @return List of predicted future positions
     */
    public static List<Vec3> predictFuturePositions(Entity entity, Vec3 currentPosition,
                                                    Vec3 previousPosition, int steps, double multiplier) {
        return predictFuturePositions(entity, currentPosition, previousPosition, steps, multiplier, 0.1);
    }

    /**
     * Predicts future positions using historical data for more accurate prediction
     *
     * @param entity           The entity to predict
     * @param currentPosition  Current position
     * @param previousPosition Previous position (from previous tick)
     * @param steps            Number of future positions to predict
     * @param multiplier       Velocity multiplier for prediction
     * @param minVelocity      Minimum velocity threshold (squared)
     * @return List of predicted future positions
     */
    public static List<Vec3> predictFuturePositions(Entity entity, Vec3 currentPosition,
                                                    Vec3 previousPosition, int steps,
                                                    double multiplier, double minVelocity) {
        List<Vec3> predictions = new ArrayList<>();

        if (entity == null || currentPosition == null || previousPosition == null || steps <= 0) {
            return predictions;
        }

        // Calculate velocity from historical positions
        Vec3 velocity = currentPosition.subtract(previousPosition).multiply(multiplier, multiplier, multiplier);

        // Check if velocity is above minimum threshold
        if (velocity.lengthSqr() < minVelocity * minVelocity) {
            return predictions;
        }

        for (int i = 1; i <= steps; i++) {
            Vec3 predictedPos = currentPosition.add(velocity.multiply(i, i, i));
            predictions.add(predictedPos);
        }

        return predictions;
    }

    /**
     * Calculates the current velocity of an entity based on historical positions
     *
     * @param currentPosition  Current position
     * @param previousPosition Previous position
     * @return Velocity vector
     */
    public static Vec3 calculateVelocity(Vec3 currentPosition, Vec3 previousPosition) {
        if (currentPosition == null || previousPosition == null) {
            return Vec3.ZERO;
        }
        return currentPosition.subtract(previousPosition);
    }

    /**
     * Checks if an entity is moving based on velocity threshold
     *
     * @param entity    The entity to check
     * @param threshold Minimum movement threshold
     * @return true if the entity is moving significantly
     */
    public static boolean isMoving(Entity entity, double threshold) {
        if (entity == null) return false;
        return entity.getDeltaMovement().lengthSqr() > threshold * threshold;
    }

    /**
     * Checks if movement is significant based on historical positions
     *
     * @param currentPosition  Current position
     * @param previousPosition Previous position
     * @param threshold        Minimum movement threshold
     * @return true if movement is significant
     */
    public static boolean isMoving(Vec3 currentPosition, Vec3 previousPosition, double threshold) {
        if (currentPosition == null || previousPosition == null) return false;
        return currentPosition.distanceToSqr(previousPosition) > threshold * threshold;
    }

    /**
     * Smooths prediction by averaging multiple historical velocities
     *
     * @param positions  List of historical positions (most recent first)
     * @param steps      Number of future positions to predict
     * @param multiplier Velocity multiplier
     * @return List of smoothed predicted positions
     */
    public static List<Vec3> smoothPredict(List<Vec3> positions, int steps, double multiplier) {
        List<Vec3> predictions = new ArrayList<>();

        if (positions == null || positions.size() < 2 || steps <= 0) {
            return predictions;
        }

        // Calculate average velocity from historical data
        Vec3 totalVelocity = Vec3.ZERO;
        int count = 0;

        for (int i = 0; i < positions.size() - 1; i++) {
            Vec3 vel = positions.get(i).subtract(positions.get(i + 1));
            totalVelocity = totalVelocity.add(vel);
            count++;
        }

        if (count == 0) {
            return predictions;
        }

        Vec3 averageVelocity = totalVelocity.multiply(1.0 / count, 1.0 / count, 1.0 / count)
                .multiply(multiplier, multiplier, multiplier);

        Vec3 currentPos = positions.get(0);

        for (int i = 1; i <= steps; i++) {
            Vec3 predictedPos = currentPos.add(averageVelocity.multiply(i, i, i));
            predictions.add(predictedPos);
        }

        return predictions;
    }
}