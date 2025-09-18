package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;
import net.minecraft.client.gui.screens.Screen;

public class OnScreen extends Event {
    public Screen screen;

    public OnScreen() {
        super(true);
    }

    public static class Open extends OnScreen {
        public static Open INSTANCE = new Open();

        public Open() {
            super();
            INSTANCE = this;
        }

        public static Open get(Screen screen) {
            INSTANCE.refresh();
            INSTANCE.screen = screen;
            return INSTANCE;
        }
    }

    public static class SignEdit extends OnScreen {
        public static SignEdit INSTANCE = new SignEdit();

        public SignEdit() {
            super();
            INSTANCE = this;
        }

        public static SignEdit get() {
            INSTANCE.refresh();

            return INSTANCE;
        }
    }
}
