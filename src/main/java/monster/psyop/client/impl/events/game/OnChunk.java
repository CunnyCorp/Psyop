package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;

public class OnChunk extends Event {
    public int x = 0;
    public int z = 0;

    public OnChunk() {
        super(false);
    }

    public static class Load extends OnChunk {
        public static Load INSTANCE = new Load();

        public static Load get(int x, int z) {
            INSTANCE.refresh();
            INSTANCE.x = x;
            INSTANCE.z = z;
            return INSTANCE;
        }
    }

    public static class Unload extends OnChunk {
        public static Unload INSTANCE = new Unload();

        public static Unload get(int x, int z) {
            INSTANCE.refresh();
            INSTANCE.x = x;
            INSTANCE.z = z;
            return INSTANCE;
        }
    }

}
