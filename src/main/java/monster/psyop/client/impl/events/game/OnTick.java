package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;

public class OnTick extends Event {

    public static class Pre extends Event {
        private static final Pre INSTANCE = new Pre();

        public static Pre get() {
            return INSTANCE;
        }
    }

    public static class Post extends Event {
        private static final Post INSTANCE = new Post();

        public static Post get() {
            return INSTANCE;
        }
    }
}
