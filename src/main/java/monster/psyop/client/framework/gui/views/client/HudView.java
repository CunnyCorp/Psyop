package monster.psyop.client.framework.gui.views.client;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiMouseButton;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.framework.gui.hud.HudElement;
import monster.psyop.client.framework.gui.hud.HudHandler;
import monster.psyop.client.framework.gui.views.View;

public class HudView extends View {
    @Override
    public String name() {
        return "hud";
    }

    public void show() {
        PersistentGuiSettings settings = Config.get().hudGui;

        if (ImGui.begin(displayName(), state())) {
            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(400, 600, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(0, 0, ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            boolean hadPriorElement = false;

            for (HudElement element : HudHandler.getElements()) {
                if (hadPriorElement) {
                    ImGui.separator();
                }

                ImGui.checkbox(element.displayName(), element.state());

                if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                    // ToDo: Add Hud Configuration View
                }

                hadPriorElement = true;
            }
        }

        settings.x = ImGui.getWindowPosX();
        settings.y = ImGui.getWindowPosY();
        settings.width = ImGui.getWindowWidth();
        settings.height = ImGui.getWindowHeight();
        ImGui.end();
    }

    @Override
    public void populateSettings(Config config) {
        settings = config.hudGui;
    }
}
