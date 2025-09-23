package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.ImVec2;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.gui.GradientUtils;

import java.awt.*;

import static monster.psyop.client.Psyop.GUI;

public class PositionHUD extends HUD {
    public ColorSetting textColor =
            new ColorSetting.Builder()
                    .name("text-color")
                    .defaultTo(new float[]{0.90f, 0.90f, 0.95f, 0.95f})
                    .addTo(coreGroup);
    public ColorSetting upperColor =
            new ColorSetting.Builder()
                    .name("upper-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public ColorSetting middleColor =
            new ColorSetting.Builder()
                    .name("middle-color")
                    .defaultTo(new float[]{0.00f, 0.60f, 0.60f, 1.0f})
                    .addTo(coreGroup);
    public ColorSetting lowerColor =
            new ColorSetting.Builder()
                    .name("lower-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public final IntSetting alpha =
            new IntSetting.Builder()
                    .name("alpha")
                    .range(40, 200)
                    .defaultTo(184)
                    .addTo(coreGroup);
    public final IntSetting waveSpeed =
            new IntSetting.Builder()
                    .name("wave-speed")
                    .range(1, 10)
                    .defaultTo(3)
                    .addTo(coreGroup);
    public final IntSetting waveDensity =
            new IntSetting.Builder()
                    .name("wave-density")
                    .range(1, 10)
                    .defaultTo(5)
                    .addTo(coreGroup);

    private final GradientUtils gradientUtils = new GradientUtils(0.5f);

    public PositionHUD() {
        super("Position", "Shows the position of the player.");
    }

    @EventListener(inGame = false)
    public void onTickPre(OnTick.Pre event) {
        gradientUtils.updateAnimation();
    }

    @EventListener(inGame = false)
    public void render(On2DRender event) {
        String position = "0, 0, 0 - (0, 0)";

        if (MC.player != null) {
            position = Math.round(MC.player.getX()) + ", " +
                    Math.round(MC.player.getY()) + ", " +
                    Math.round(MC.player.getZ()) + " - (" +
                    Math.round(MC.player.getX() / 8) + ", " +
                    Math.round(MC.player.getZ() / 8) + ")";
        }

        ImVec2 textSize = new ImVec2();
        ImGui.calcTextSize(textSize, position);

        float padding = 8f;
        float bgWidth = textSize.x + (padding * 2);
        float bgHeight = textSize.y + (padding * 2);

        float bgX = xPos.get() - padding;
        float bgY = yPos.get() - padding;

        Color[] waveColors = {
                GradientUtils.getColorFromSetting(upperColor),
                GradientUtils.getColorFromSetting(middleColor),
                GradientUtils.getColorFromSetting(lowerColor),
                GradientUtils.getColorFromSetting(middleColor)
        };

        gradientUtils.drawHorizontalWaveGradientTile(
                bgX, bgY, bgWidth, bgHeight,
                waveColors, alpha.get(),
                waveSpeed.get() / 2f,
                waveDensity.get() / 2f
        );

        float textX = bgX + padding;
        float textY = bgY + padding;

        GUI.drawString(position, textX, textY, new ImColorW(textColor.get()));
    }
}