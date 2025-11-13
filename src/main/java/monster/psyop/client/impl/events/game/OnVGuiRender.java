package monster.psyop.client.impl.events.game;

import lombok.Getter;
import monster.psyop.client.framework.events.Event;
import net.minecraft.client.gui.GuiGraphics;

public class OnVGuiRender extends Event {
    public static final OnVGuiRender INSTANCE = new OnVGuiRender();
    @Getter
    private GuiGraphics guiGraphics;

    public OnVGuiRender() {
        super(false);
    }

    public static OnVGuiRender get(GuiGraphics guiGraphics) {
        INSTANCE.guiGraphics = guiGraphics;
        return INSTANCE;
    }
}
