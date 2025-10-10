package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.ImVec2;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.gui.GradientUtils;

import java.awt.*;

import static monster.psyop.client.Psyop.GUI;

public class HungerBarHUD extends HUD {
    // Appearance
    public final ColorSetting textColor = new ColorSetting.Builder()
            .name("text-color")
            .defaultTo(new float[]{0.95f, 0.95f, 1.0f, 1.0f})
            .addTo(coreGroup);
    public final ColorSetting upperColor = new ColorSetting.Builder()
            .name("upper-color")
            .defaultTo(new float[]{1.00f, 0.65f, 0.00f, 1.0f}) // orange
            .addTo(coreGroup);
    public final ColorSetting middleColor = new ColorSetting.Builder()
            .name("middle-color")
            .defaultTo(new float[]{0.95f, 0.50f, 0.00f, 1.0f})
            .addTo(coreGroup);
    public final ColorSetting lowerColor = new ColorSetting.Builder()
            .name("lower-color")
            .defaultTo(new float[]{0.80f, 0.30f, 0.00f, 1.0f})
            .addTo(coreGroup);

    public final IntSetting alpha = new IntSetting.Builder()
            .name("alpha")
            .range(40, 255)
            .defaultTo(200)
            .addTo(coreGroup);

    // Layout
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
    public final BoolSetting showSaturation = new BoolSetting.Builder()
            .name("show-saturation-overlay")
            .defaultTo(true)
            .addTo(coreGroup);

    private final GradientUtils gradientUtils = new GradientUtils(0.5f);

    public HungerBarHUD() {
        super("HungerBar", "Displays your current hunger as a bar.");
    }

    @EventListener(inGame = false)
    public void onTickPre(OnTick.Pre event) {
        gradientUtils.updateAnimation();
    }

    @EventListener(inGame = false)
    public void on2D(On2DRender e) {
        if (MC.player == null) return;

        // Food stats
        var foodData = MC.player.getFoodData();
        int foodLevel = foodData.getFoodLevel(); // 0..20
        float saturation = foodData.getSaturationLevel(); // can exceed food level but typically 0..20

        float current = Math.max(0, foodLevel);
        float max = 20f;
        float pct = Math.min(1f, current / max);

        float x = xPos.get();
        float y = yPos.get();
        float w = width.get();
        float h = height.get();

        // Background tile using a horizontal wave gradient
        Color[] waveColors = new Color[]{
                GradientUtils.getColorFromSetting(lowerColor),
                GradientUtils.getColorFromSetting(middleColor),
                GradientUtils.getColorFromSetting(upperColor),
                GradientUtils.getColorFromSetting(middleColor)
        };
        float bgX = x - 4f;
        float bgY = y - 6f;
        float bgW = w + 8f;
        float bgH = h + 12f;
        gradientUtils.drawHorizontalWaveGradientTile(bgX, bgY, bgW, bgH, waveColors, alpha.get(), 2.0f, 0.6f);

        // Main hunger fill
        float filledW = Math.max(0f, Math.min(w, w * pct));
        int fillColor = new ImColorW(GradientUtils.getMultiGradientColor(new Color[]{
                GradientUtils.getColorFromSetting(lowerColor),
                GradientUtils.getColorFromSetting(middleColor),
                GradientUtils.getColorFromSetting(upperColor)
        }, pct, 220)).packed();

        ImGui.getBackgroundDrawList().addRectFilled(x, y, x + w, y + h, new ImColorW(new Color(0, 0, 0, 120)).packed(), 3f, 0);
        ImGui.getBackgroundDrawList().addRectFilled(x, y, x + filledW, y + h, fillColor, 3f, 0);

        // Optional saturation overlay: thin bar on top showing saturation up to current food (clamped)
        if (showSaturation.get()) {
            float satShown = Math.min(max, Math.max(0f, saturation));
            float satPct = Math.min(1f, satShown / max);
            float satW = Math.max(0f, Math.min(w, w * satPct));
            int satColor = new ImColorW(new Color(255, 255, 255, 110)).packed();
            float overlayH = Math.max(2f, Math.min(h - 2f, 3f));
            ImGui.getBackgroundDrawList().addRectFilled(x, y, x + satW, y + overlayH, satColor, 2f, 0);
        }

        if (showNumbers.get()) {
            String txt = foodLevel + "/" + (int) max;
            if (showSaturation.get()) {
                txt += " (" + Math.round(saturation) + ")";
            }
            ImVec2 size = new ImVec2();
            ImGui.calcTextSize(size, txt);
            float tx = x + (w - size.x) / 2f;
            float ty = y + (h - size.y) / 2f;
            GUI.drawString(txt, tx, ty, new ImColorW(textColor.get()).packed());
        }
    }
}
