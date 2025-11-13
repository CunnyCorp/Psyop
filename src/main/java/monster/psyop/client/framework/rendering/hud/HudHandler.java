package monster.psyop.client.framework.rendering.hud;

import imgui.ImGui;
import imgui.ImVec2;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.hud.HUD;

import java.util.ArrayList;
import java.util.List;


public class HudHandler {
    public static final List<HUD> HUD_LIST = new ArrayList<>();

    public static void addHud(HUD hud) {
        HUD_LIST.add(hud);
        Psyop.log("Adding the hud element: {}", hud.name);
    }

    public static HUD getHoveredHud() {
        ImVec2 pos = ImGui.getMousePos();

        for (HUD hud : HUD_LIST) {
            if (hud.getWidth() <= 0 || hud.getHeight() <= 0) {
                continue;
            }

            if (hud.active()) {
                int x = hud.xPos.get();
                int y = hud.yPos.get();
                int width = hud.getWidth();
                int height = hud.getHeight();

                if ((hud.leftAligned() ? pos.x >= x && pos.x <= x + width : pos.x <= x && pos.x <= x - width) && pos.y >= y && pos.y <= y + height) {
                    return hud;
                }
            }
        }
        return null;
    }

    public static int getRenderX(HUD hud) {
        if (hud.leftAligned()) {
            return hud.xPos.get();
        } else {
            return hud.xPos.get() - hud.getWidth();
        }
    }
}