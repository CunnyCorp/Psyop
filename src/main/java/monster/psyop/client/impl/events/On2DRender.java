package monster.psyop.client.impl.events;

import monster.psyop.client.framework.events.Event;

public class On2DRender extends Event {
    public static On2DRender INSTANCE = new On2DRender();

    public On2DRender() {
        super(false);
    }

    public static On2DRender get() {
        return INSTANCE;
    }
}
