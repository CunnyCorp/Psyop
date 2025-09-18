package monster.psyop.client.framework.modules.settings.ranges;

public record IntRange(int min, int max) {
    public int correct(int v) {
        if (v < min) {
            return min;
        }
        return Math.min(v, max);
    }
}
