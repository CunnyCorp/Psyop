package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;

public class OnRender extends Event {
    public static OnRender INSTANCE = new OnRender();

    public OnRender() {
        super(false);
    }

    public static OnRender get() {
        return INSTANCE;
    }
}
