package monster.psyop.client.framework.gui.views.features;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import monster.psyop.client.config.Config;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.util.Objects;

import static monster.psyop.client.Liberty.MC;

public class TrollingView extends View {

    @Override
    public String name() {
        return "trolling";
    }

    @Override
    public void show() {
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

            if (ImGui.button("Blank Chat Msg")) {
                Objects.requireNonNull(MC.getConnection()).sendChat(" ");
            }

            ImGui.sameLine();

            if (ImGui.button("Empty Signed Command")) {
                Objects.requireNonNull(MC.getConnection()).sendCommand("");
            }

            ImGui.sameLine();

            if (ImGui.button("Empty Command")) {
                PacketUtils.send(new ServerboundChatCommandPacket(""));
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
        settings = config.trollingGui;
    }
}
