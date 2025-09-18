package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;

// This run's after module calls, for sanity purposes.
public class OnKeyInput extends Event {
    public static OnKeyInput INSTANCE = new OnKeyInput();
    public int key = 0;
    public int action = 0;

    public OnKeyInput() {
        super(true);
    }

    public static OnKeyInput get(int key, int action) {
        INSTANCE.refresh();
        INSTANCE.key = key;
        INSTANCE.action = action;
        return INSTANCE;
    }
}
