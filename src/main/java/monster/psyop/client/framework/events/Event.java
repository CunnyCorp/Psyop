package monster.psyop.client.framework.events;

import lombok.Getter;

public class Event {
    private static String id;
    @Getter
    private final boolean cancellable;
    private boolean cancel = false;

    public Event() {
        this(false);
    }

    public Event(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public boolean cancel() {
        if (cancellable) {
            cancel = !cancel;
            return cancel;
        }
        return false;
    }

    public boolean isCancelled() {
        return cancellable && cancel;
    }

    protected void refresh() {
        this.cancel = false;
    }
}
