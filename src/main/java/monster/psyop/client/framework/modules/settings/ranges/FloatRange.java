package monster.psyop.client.framework.modules.settings.ranges;

public record FloatRange(float min, float max) {
    public float correct(float v) {
        if (v < min) {
            return min;
        }
        return Math.min(v, max);
    }
}
