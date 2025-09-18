package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;

import java.util.List;

public class OnBookSign extends Event {
    public static OnBookSign INSTANCE = new OnBookSign();
    public List<String> pages;
    public boolean stop = false;

    public OnBookSign() {
        super(true);
    }

    public static OnBookSign get(List<String> pages) {
        INSTANCE.refresh();
        INSTANCE.stop = false;
        INSTANCE.pages = pages;
        return INSTANCE;
    }

    public void stop() {
        stop = true;
    }
}
