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

public class HealthBarHUD extends HUD {
    // Appearance
    public final ColorSetting textColor = new ColorSetting.Builder()
            .name("text-color")
            .defaultTo(new float[]{0.95f, 0.95f, 1.0f, 1.0f})
            .addTo(coreGroup);
    public final ColorSetting upperColor = new ColorSetting.Builder()
            .name("upper-color")
            .defaultTo(new float[]{0.00f, 0.80f, 0.25f, 1.0f})
            .addTo(coreGroup);
    public final ColorSetting middleColor = new ColorSetting.Builder()
            .name("middle-color")
            .defaultTo(new float[]{0.95f, 0.80f, 0.10f, 1.0f})
            .addTo(coreGroup);
    public final ColorSetting lowerColor = new ColorSetting.Builder()
            .name("lower-color")
            .defaultTo(new float[]{0.90f, 0.10f, 0.10f, 1.0f})
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
    public final BoolSetting includeAbsorption = new BoolSetting.Builder()
            .name("include-absorption")
            .defaultTo(true)
            .addTo(coreGroup);

    private final GradientUtils gradientUtils = new GradientUtils(0.5f);

    public HealthBarHUD() {
        super("HealthBar", "Displays your current health as a bar.");
    }

    @EventListener(inGame = false)
    public void onTickPre(OnTick.Pre event) {
        gradientUtils.updateAnimation();
    }

    @EventListener(inGame = false)
    public void on2D(On2DRender e) {
        if (MC.player == null) return;

        float health = MC.player.getHealth();
        float maxHealth = MC.player.getMaxHealth();
        float absorption = includeAbsorption.get() ? MC.player.getAbsorptionAmount() : 0f;

        float current = Math.max(0f, health + absorption);
        float max = Math.max(1f, maxHealth + (includeAbsorption.get() ? absorption : 0f));
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

        // Health bar fill
        float filledW = Math.max(0f, Math.min(w, w * pct));
        int fillColor = new ImColorW(GradientUtils.getMultiGradientColor(new Color[]{
                GradientUtils.getColorFromSetting(lowerColor),
                GradientUtils.getColorFromSetting(middleColor),
                GradientUtils.getColorFromSetting(upperColor)
        }, pct, 220)).packed();

        ImGui.getBackgroundDrawList().addRectFilled(x, y, x + w, y + h, new ImColorW(new Color(0, 0, 0, 120)).packed(), 3f, 0);
        ImGui.getBackgroundDrawList().addRectFilled(x, y, x + filledW, y + h, fillColor, 3f, 0);

        if (showNumbers.get()) {
            int hpInt = Math.round(current);
            int maxInt = Math.round(maxHealth + (includeAbsorption.get() ? absorption : 0f));
            String txt = hpInt + "/" + maxInt;
            ImVec2 size = new ImVec2();
            ImGui.calcTextSize(size, txt);
            float tx = x + (w - size.x) / 2f;
            float ty = y + (h - size.y) / 2f;
            GUI.drawString(txt, tx, ty, new ImColorW(textColor.get()).packed());
        }
    }
}
