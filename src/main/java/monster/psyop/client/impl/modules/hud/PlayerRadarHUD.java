package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.StringSetting;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.Comparator;
import java.util.List;

public class PlayerRadarHUD extends HUD {
    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    public final ColorSetting textColor =
            new ColorSetting.Builder()
                    .name("text-color")
                    .defaultTo(new float[]{1f, 1f, 1f, 1f})
                    .addTo(coreGroup);
    public final ColorSetting highHealth =
            new ColorSetting.Builder()
                    .name("high-health")
                    .defaultTo(new float[]{0.0f, 0.0f, 1.0f, 1.0f})
                    .addTo(coreGroup);
    public final ColorSetting mediumHealth =
            new ColorSetting.Builder()
                    .name("medium-health")
                    .defaultTo(new float[]{1.0f, 1.0f, 0.0f, 1.0f})
                    .addTo(coreGroup);
    public final ColorSetting lowHealth =
            new ColorSetting.Builder()
                    .name("low-health")
                    .defaultTo(new float[]{1.0f, 0.0f, 0.0f, 1.0f})
                    .addTo(coreGroup);
    public final StringSetting hpText =
            new StringSetting.Builder()
                    .name("hp-text")
                    .defaultTo("<3")
                    .addTo(coreGroup);
    public final BoolSetting autoHide =
            new BoolSetting.Builder()
                    .name("auto-hide")
                    .defaultTo(true)
                    .addTo(coreGroup);

    public PlayerRadarHUD() {
        super("player-radar", "Shows nearby players with distance and health.");
    }

    @Override
    public void render() {
        if (Psyop.MC.level == null || Psyop.MC.player == null) return;

        List<AbstractClientPlayer> players = Psyop.MC.level.players().stream()
                .filter(p -> p != Psyop.MC.player)
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(Psyop.MC.player)))
                .toList();

        if (autoHide.get() && players.isEmpty()) return;

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
                        hpText.value().get()
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

    private float[] getHealthColor(float health) {
        if (health <= 6) {
            return lowHealth.get();
        } else if (health <= 14) {
            return mediumHealth.get();
        } else {
            return highHealth.get();
        }
    }
}