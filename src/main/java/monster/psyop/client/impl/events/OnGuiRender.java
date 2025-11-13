package monster.psyop.client.impl.events;

import imgui.ImDrawList;
import imgui.ImGui;
import monster.psyop.client.framework.events.Event;

public class OnGuiRender extends Event {
    public static OnGuiRender INSTANCE = new OnGuiRender();

    public OnGuiRender() {
        super(false);
    }

    public ImDrawList drawList() {
        return ImGui.getBackgroundDrawList();
    }

    public static OnGuiRender get() {
        return INSTANCE;
    }
}
