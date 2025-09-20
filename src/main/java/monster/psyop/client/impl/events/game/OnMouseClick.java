package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;

public class OnMouseClick extends Event {
    public static OnMouseClick INSTANCE = new OnMouseClick();
    public int key = 0;
    public int action = 0;

    public OnMouseClick() {
        super(true);
    }

    public static OnMouseClick get(int key, int action) {
        INSTANCE.refresh();
        INSTANCE.key = key;
        INSTANCE.action = action;
        return INSTANCE;
    }
}
