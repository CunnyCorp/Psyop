package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;
import net.minecraft.network.chat.Component;

public class OnGameQuit extends Event {
    public static OnGameQuit INSTANCE = new OnGameQuit();
    public Component reason;

    public OnGameQuit() {
        super(true);
    }

    public static OnGameQuit get(Component component) {
        INSTANCE.refresh();
        INSTANCE.reason = component;
        return INSTANCE;
    }
}
