package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnPacket;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public class TargetHUD extends HUD {

    public final IntSetting scale =
            new IntSetting.Builder()
                    .name("scale")
                    .range(50, 200)
                    .defaultTo(100)
                    .addTo(coreGroup);

    public final ColorSetting textColor =
            new ColorSetting.Builder()
                    .name("text-color")
                    .defaultTo(new float[]{1f, 1f, 1f, 1f})
                    .addTo(coreGroup);

    public final IntSetting maxTargetDistance =
            new IntSetting.Builder()
                    .name("max-distance")
                    .range(10, 200)
                    .defaultTo(50)
                    .addTo(coreGroup);

    private AbstractClientPlayer target;
    private long lastHitTime;

    public TargetHUD() {
        super("target-hud", "Shows detailed information about your current target.");
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundInteractPacket packet) {
            if (packet.action.getType() == ServerboundInteractPacket.ActionType.ATTACK) {
                if (MC.level.getEntity(packet.entityId) instanceof AbstractClientPlayer) {
                    this.target = (AbstractClientPlayer) MC.level.getEntity(packet.entityId);
                    this.lastHitTime = System.currentTimeMillis();
                }
            }
        }
    }

    @EventListener
    public void render(On2DRender event) {
        if (Psyop.MC.level == null || Psyop.MC.player == null) {
            target = null;
            return;
        }

        if (System.currentTimeMillis() - lastHitTime > 15000) {
            target = null;
        }

        if (target == null || !target.isAlive() || Psyop.MC.player.distanceTo(target) > maxTargetDistance.get()) {
            target = null;
            return;
        }

        float uiScale = scale.get() / 100.0f;

        ImGui.setNextWindowSize(280 * uiScale, 120 * uiScale);
        ImGui.begin("Target HUD", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8 * uiScale, 8 * uiScale);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.1f, 0.1f, 0.1f, 0.8f);

        float[] tc = textColor.get();

        ImGui.beginGroup();
        ImGui.endGroup();

        ImGui.sameLine();

        ImGui.beginGroup();

        // NAME
        ImGui.textColored(tc[0], tc[1], tc[2], tc[3],
                "Target: " + target.getName().getString());

        ImGui.separator();

        // HEALTH
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float healthPercent = health / maxHealth;
        String healthText = String.format("Health: %.1f / %.1f", health, maxHealth);

        float[] healthColor = getHealthColor(health);
        ImGui.textColored(tc[0], tc[1], tc[2], tc[3], healthText);

        ImGui.pushStyleColor(ImGuiCol.PlotHistogram, healthColor[0], healthColor[1], healthColor[2], healthColor[3]);
        ImGui.progressBar(
                healthPercent,
                ImGui.getContentRegionAvailX(),
                18 * uiScale,
                String.format("%.0f%%", healthPercent * 100)
        );
        ImGui.popStyleColor();

        // DISTANCE
        int distance = (int) Psyop.MC.player.distanceTo(target);
        float[] distColor = distance <= 5
                ? new float[]{1f, 0.2f, 0.2f, 1f}
                : new float[]{0.5f, 1f, 0.5f, 1f};
        ImGui.textColored(distColor[0], distColor[1], distColor[2], distColor[3],
                "Distance: " + distance + "m");

        // PING
        int ping = getPing(target);
        ImGui.textColored(tc[0], tc[1], tc[2], tc[3],
                "Ping: " + ping + "ms");

        // ARMOR
        int armor = target.getArmorValue();
        ImGui.textColored(tc[0], tc[1], tc[2], tc[3],
                "Armor: " + armor);

        ImGui.endGroup();

        ImGui.popStyleColor();
        ImGui.popStyleVar();
        ImGui.end();
    }

    private int getPing(AbstractClientPlayer player) {
        if (Psyop.MC.getConnection() != null) {
            var playerInfo = Psyop.MC.getConnection().getPlayerInfo(player.getUUID());
            return playerInfo != null ? playerInfo.getLatency() : 0;
        }
        return 0;
    }

    private float[] getHealthColor(float health) {
        if (health <= 6) {
            return new float[]{1f, 0f, 0f, 1f};
        } else if (health <= 14) {
            return new float[]{1f, 1f, 0f, 1f};
        } else {
            return new float[]{0f, 1f, 0f, 1f};
        }
    }
}
