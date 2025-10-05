package monster.psyop.client.utility.gui;

public class NotificationEvent {
    public final String title;
    public final String message;
    public final Type type;
    public final long duration;
    public final long startTime;

    public NotificationEvent(String title, String message, Type type, long duration) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public enum Type {
        INFO(new float[]{0.00f, 0.75f, 0.75f, 1.0f}),
        SUCCESS(new float[]{0.00f, 0.75f, 0.00f, 1.0f}),
        WARNING(new float[]{0.75f, 0.75f, 0.00f, 1.0f}),
        ERROR(new float[]{0.75f, 0.00f, 0.00f, 1.0f}),
        DEBUG(new float[]{0.50f, 0.50f, 0.50f, 1.0f});

        public final float[] color;

        Type(float[] color) {
            this.color = color;
        }
    }

    public float getProgress(long currentTime) {
        long elapsed = currentTime - startTime;
        return Math.min(1.0f, (float) elapsed / duration);
    }

    public boolean isExpired(long currentTime) {
        return currentTime - startTime > duration;
    }
}