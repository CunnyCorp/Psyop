package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public abstract class OnRenderSlot extends Event {
    public GuiGraphics guiGraphics;
    public ItemStack stack;
    public int x;
    public int y;

    public OnRenderSlot(boolean cancellable) {
        super(cancellable);
    }

    public static class Pre extends OnRenderSlot {
        public static Pre INSTANCE = new Pre();

        public Pre() {
            super(true);
            INSTANCE = this;
        }

        public static Pre get(GuiGraphics guiGraphics, ItemStack st, int x, int y) {
            INSTANCE.refresh();
            INSTANCE.guiGraphics = guiGraphics;
            INSTANCE.stack = st;
            INSTANCE.x = x;
            INSTANCE.y = y;
            return INSTANCE;
        }
    }

    public static class Post extends OnRenderSlot {
        public static Post INSTANCE = new Post();

        public Post() {
            super(false);
            INSTANCE = this;
        }

        public static Post get(GuiGraphics guiGraphics, ItemStack st, int x, int y) {
            INSTANCE.guiGraphics = guiGraphics;
            INSTANCE.stack = st;
            INSTANCE.x = x;
            INSTANCE.y = y;
            return INSTANCE;
        }
    }
}
