package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.ImVec2;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.gui.utility.GuiUtils;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.DimensionCheck;
import monster.psyop.client.utility.gui.GradientUtils;

import java.awt.*;

import static monster.psyop.client.Psyop.GUI;

public class PositionHUD extends HUD {
    public final ColorSetting textColor = new ColorSetting.Builder()
            .name("text-color")
            .defaultTo(new float[]{0.95f, 0.95f, 1.0f, 1.0f})
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
                    Math.round(MC.player.getZ());

            try {
                if (DimensionCheck.NETHER.check.call()) {
                    position += " - (" +
                            Math.round(MC.player.getX() * 8) + ", " +
                            Math.round(MC.player.getZ() * 8) + ")";
                } else if (DimensionCheck.OW.check.call()) {
                    position += " - (" +
                            Math.round(MC.player.getX() / 8) + ", " +
                            Math.round(MC.player.getZ() / 8) + ")";
                }
            } catch (Exception ignored) {
            }

        }

        ImVec2 textSize = new ImVec2();
        ImGui.calcTextSize(textSize, position);

        float padding = 8f;
        float bgW = textSize.x + (padding * 2);
        float bgH = textSize.y + (padding * 2);

        float bgX = xPos.get() - padding;
        float bgY = yPos.get() - padding;

        GUI.drawBackground(bgX, bgY, bgX + bgW, bgY + bgH);

        float textX = bgX + padding;
        float textY = bgY + padding;

        GUI.drawString(position, textX, textY, new ImColorW(textColor.get()));
    }
}