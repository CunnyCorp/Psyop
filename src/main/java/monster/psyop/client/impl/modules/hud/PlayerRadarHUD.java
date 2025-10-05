package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.On2DRender;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerRadarHUD extends HUD {

    public final ColorSetting textColor =
            new ColorSetting.Builder()
                    .name("text-color")
                    .defaultTo(new float[]{1f, 1f, 1f, 1f})
                    .addTo(coreGroup);

    public PlayerRadarHUD() {
        super("player-radar", "Shows nearby players with distance and health.");
    }

    @EventListener
    public void render(On2DRender event) {
        if (Psyop.MC.level == null || Psyop.MC.player == null) return;

        List<AbstractClientPlayer> players = Psyop.MC.level.players().stream()
                .filter(p -> p != Psyop.MC.player)
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(Psyop.MC.player)))
                .collect(Collectors.toList());

        if (players.isEmpty()) return;

        ImGui.begin("Player Radar", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.AlwaysAutoResize);

        for (AbstractClientPlayer p : players) {
            int dist = (int) Psyop.MC.player.distanceTo(p);
            float health = p.getHealth();
            int healthInt = Math.round(health);
            float[] healthColor = getHealthColor(health);

            String formatted = String.format("%-15s %4dm %3d",
                    p.getName().getString(), dist, healthInt);

            String healthPart = String.valueOf(healthInt);
            int healthIndex = formatted.lastIndexOf(healthPart);

            if (healthIndex > 0) {
                ImGui.textColored(
                        textColor.get()[0], textColor.get()[1], textColor.get()[2], textColor.get()[3],
                        formatted.substring(0, healthIndex)
                );
                ImGui.sameLine();
                ImGui.textColored(
                        healthColor[0], healthColor[1], healthColor[2], healthColor[3],
                        healthPart
                );
                ImGui.sameLine();
                ImGui.textColored(
                        healthColor[0], healthColor[1], healthColor[2], healthColor[3],
                        "<3"
                );
            } else {
                ImGui.textColored(
                        textColor.get()[0], textColor.get()[1], textColor.get()[2], textColor.get()[3],
                        formatted
                );
            }
        }

        ImGui.end();
    }

    /**
     * Returns color based on health value:
     * - Red for low health (0-6)
     * - Yellow for medium health (7-14)
     * - Green for high health (15-20)
     */
    private float[] getHealthColor(float health) {
        if (health <= 6) {
            return new float[]{1.0f, 0.0f, 0.0f, 1.0f}; // Red
        } else if (health <= 14) {
            return new float[]{1.0f, 1.0f, 0.0f, 1.0f}; // Yellow
        } else {
            return new float[]{0.0f, 1.0f, 0.0f, 1.0f}; // Green
        }
    }
}