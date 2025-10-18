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

public class HungerBarHUD extends HUD {
    public final ColorSetting textColor = new ColorSetting.Builder()
            .name("text-color")
            .defaultTo(new float[]{0.95f, 0.95f, 1.0f, 1.0f})
            .addTo(coreGroup);
    public final ColorSetting hungerColor = new ColorSetting.Builder()
            .name("hunger-color")
            .defaultTo(new float[]{0.3f, 0.3f, 0.0f, 1.0f})
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

    public HungerBarHUD() {
        super("HungerBar", "Displays your current hunger as a bar.");
    }

    @EventListener(inGame = false)
    public void on2D(On2DRender e) {
        if (MC.player == null) return;

        float foodLevel = MC.player.getFoodData().getFoodLevel();

        float current = Math.max(0f, foodLevel);
        float pct = Math.min(1f, current / 20);

        float x = xPos.get();
        float y = yPos.get();
        float w = width.get();
        float h = height.get();
        float bgX = x - 4f;
        float bgY = y - 6f;
        float bgW = w + 8f;
        float bgH = h + 12f;

        GUI.drawBackground(bgX, bgY, bgW, bgH);

        float filledW = Math.max(0f, Math.min(w, w * pct));
        int fColor = ImColorW.packed(hungerColor.get());

        ImGui.getBackgroundDrawList().addRectFilled(x, y, x + w, y + h, new ImColorW(new Color(0, 0, 0, 120)).packed(), 3f, 0);
        ImGui.getBackgroundDrawList().addRectFilled(x, y, x + filledW, y + h, fColor, 3f, 0);

        if (showNumbers.get()) {
            int hpInt = Math.round(current);
            String txt = hpInt + "/" + 20;
            ImVec2 size = new ImVec2();
            ImGui.calcTextSize(size, txt);
            float tx = x + (w - size.x) / 2f;
            float ty = y + (h - size.y) / 2f;
            GUI.drawString(txt, tx, ty, new ImColorW(textColor.get()).packed());
        }
    }
}
