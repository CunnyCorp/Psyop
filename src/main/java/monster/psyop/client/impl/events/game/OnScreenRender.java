package monster.psyop.client.impl.events.game;

import lombok.Getter;
import monster.psyop.client.framework.events.Event;
import net.minecraft.client.gui.GuiGraphics;

public class OnScreenRender extends Event {
    public static final OnScreenRender INSTANCE = new OnScreenRender();
    @Getter
    private GuiGraphics guiGraphics;

    public OnScreenRender() {
        super(false);
    }

    public static OnScreenRender get(GuiGraphics guiGraphics) {
        INSTANCE.guiGraphics = guiGraphics;
        return INSTANCE;
    }
}
