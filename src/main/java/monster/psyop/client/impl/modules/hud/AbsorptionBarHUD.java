package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.gui.utility.GuiUtils;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.On2DRender;

import java.awt.*;

import static monster.psyop.client.Psyop.GUI;

public class AbsorptionBarHUD extends HUD {
    public final ColorSetting textColor = new ColorSetting.Builder()
            .name("text-color")
            .defaultTo(new float[]{0.95f, 0.95f, 1.0f, 1.0f})
            .addTo(coreGroup);
    public final ColorSetting healthColor = new ColorSetting.Builder()
            .name("health-color")
            .defaultTo(new float[]{0.0f, 0.2f, 0.4f, 1.0f})
            .addTo(coreGroup);
    public final IntSetting width = new IntSetting.Builder()
            .name("width")
            .range(50, 400)
            .defaultTo(160)
            .addTo(coreGroup);
    public final IntSetting height = new IntSetting.Builder()
            .name("height")
            .range(6, 40)
            .defaultTo(14)
            .addTo(coreGroup);
    public final BoolSetting showNumbers = new BoolSetting.Builder()
            .name("show-numbers")
            .defaultTo(true)
            .addTo(coreGroup);

    public AbsorptionBarHUD() {
        super("AbsorptionBar", "Displays your current absorption as a bar.");
    }

    @EventListener(inGame = false)
    public void on2D(On2DRender e) {
        if (MC.player == null) return;

        float absorption = MC.player.getAbsorptionAmount();
        float maxAbsorption = MC.player.getMaxAbsorption();

        if (maxAbsorption <= 0) {
            return;
        }

        float current = Math.max(0f, absorption);
        float pct = Math.min(1f, current / maxAbsorption);

        float x = xPos.get();
        float y = yPos.get();
        float w = width.get();
        float h = height.get();
        float bgX = x - 4f;
        float bgY = y - 6f;
        float bgW = w + 8f;
        float bgH = h + 12f;

        GUI.drawBackground(bgX, bgY, bgX + bgW, bgY + bgH);

        float filledW = Math.max(0f, Math.min(w, w * pct));
        int fColor = ImColorW.packed(healthColor.get());

        ImGui.getBackgroundDrawList().addRectFilled(x, y, x + w, y + h, ImGuiCol.Border, 3f, 0);
        ImGui.getBackgroundDrawList().addRectFilled(x, y, x + filledW, y + h, fColor, 3f, 0);

        if (showNumbers.get()) {
            int hpInt = Math.round(current);
            int maxInt = Math.round(maxAbsorption);
            String txt = hpInt + "/" + maxInt;
            ImVec2 size = new ImVec2();
            ImGui.calcTextSize(size, txt);
            float tx = x + (w - size.x) / 2f;
            float ty = y + (h - size.y) / 2f;
            GUI.drawString(txt, tx, ty, new ImColorW(textColor.get()).packed());
        }
    }
}
