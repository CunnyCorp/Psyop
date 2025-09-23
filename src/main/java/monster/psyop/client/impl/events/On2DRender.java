package monster.psyop.client.impl.events;

import imgui.ImDrawList;
import imgui.ImGui;
import monster.psyop.client.framework.events.Event;

public class On2DRender extends Event {
    public static On2DRender INSTANCE = new On2DRender();

    public On2DRender() {
        super(false);
    }

    public ImDrawList drawList() {
        return ImGui.getBackgroundDrawList();
    }

    public static On2DRender get() {
        return INSTANCE;
    }
}
