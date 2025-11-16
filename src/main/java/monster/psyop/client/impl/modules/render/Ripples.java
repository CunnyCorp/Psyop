package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class Ripples extends Module {
    public GroupedSettings general = addGroup(new GroupedSettings("general", "General settings"));
    public GroupedSettings targets = addGroup(new GroupedSettings("targets", "Whose trails to render"));
    public GroupedSettings style = addGroup(new GroupedSettings("style", "Visual style"));

    public IntSetting trailLength = new IntSetting.Builder()
            .name("trail-length")
            .description("Max positions stored per player")
            .defaultTo(20)
            .range(1, 100)
            .addTo(general);
    public IntSetting updateRate = new IntSetting.Builder()
            .name("update-rate")
            .description("Update interval in ticks")
            .defaultTo(2)
            .range(1, 20)
            .addTo(general);
    public FloatSetting minDistance = new FloatSetting.Builder()
            .name("min-distance")
            .description("Minimum movement to record a new point")
            .defaultTo(0.1f)
            .range(0.01f, 5.0f)
            .addTo(general);
    public FloatSetting renderDistance = new FloatSetting.Builder()
            .name("render-distance")
            .description("Only record/render players within this distance")
            .defaultTo(64.0f)
            .range(1.0f, 512.0f)
            .addTo(general);
    public FloatSetting maxAgeSeconds = new FloatSetting.Builder()
            .name("max-age-seconds")
            .description("Remove trail points older than this")
            .defaultTo(30.0f)
            .range(1.0f, 120.0f)
            .addTo(general);

    public BoolSetting self = new BoolSetting.Builder()
            .name("self")
            .defaultTo(true)
            .addTo(targets);
    public BoolSetting others = new BoolSetting.Builder()
            .name("others")
            .defaultTo(true)
            .addTo(targets);

    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.9f})
            .addTo(style);
    public ColorSetting friendColor = new ColorSetting.Builder()
            .name("friend-color")
            .defaultTo(new float[]{0.0f, 0.6f, 1.0f, 0.9f})
            .addTo(style);
    public FloatSetting selfRadius = new FloatSetting.Builder()
            .name("self-radius")
            .defaultTo(0.5f)
            .range(0.1f, 3.0f)
            .addTo(style);
    public FloatSetting othersRadius = new FloatSetting.Builder()
            .name("others-radius")
            .defaultTo(0.4f)
            .range(0.1f, 3.0f)
            .addTo(style);
    public IntSetting circleSegments = new IntSetting.Builder()
            .name("circle-segments")
            .defaultTo(24)
            .range(8, 96)
            .addTo(style);
    public FloatSetting lineWidth = new FloatSetting.Builder()
            .name("line-width")
            .defaultTo(2.0f)
            .range(1.0f, 6.0f)
            .addTo(style);
    public BoolSetting fadeWithTime = new BoolSetting.Builder()
            .name("fade-with-time")
            .defaultTo(true)
            .addTo(style);

    public BoolSetting glow = new BoolSetting.Builder()
            .name("glow")
            .defaultTo(true)
            .addTo(style);
    public FloatSetting glowWidth = new FloatSetting.Builder()
            .name("glow-width")
            .description("How far the glow extends outwards from the circle")
            .defaultTo(0.35f)
            .range(0.05f, 1.0f)
            .addTo(style);
    public IntSetting glowSteps = new IntSetting.Builder()
            .name("glow-steps")
            .description("How many concentric glow rings to draw")
            .defaultTo(4)
            .range(1, 12)
            .addTo(style);
    public FloatSetting glowAlpha = new FloatSetting.Builder()
            .name("glow-alpha")
            .description("Base alpha of the glow rings")
            .defaultTo(0.35f)
            .range(0.0f, 1.0f)
            .addTo(style);
    public FloatSetting glowPulseSpeed = new FloatSetting.Builder()
            .name("glow-pulse-speed")
            .description("Speed of glow pulsing (0 to disable)")
            .defaultTo(1.0f)
            .range(0.0f, 5.0f)
            .addTo(style);

    public Ripples() {
        super(Categories.RENDER, "ripples", "Shows circle ripples at player positions with a glowing effect.");
    }

    private static class TrailPos {
        final Vec3 pos;
        final long timeMs;

        TrailPos(Vec3 pos) {
            this.pos = pos;
            this.timeMs = System.currentTimeMillis();
        }

        float ageSeconds() {
            return (System.currentTimeMillis() - timeMs) / 1000.0f;
        }
    }

    private final Map<UUID, Deque<TrailPos>> trails = new HashMap<>();
    private final Map<UUID, Vec3> lastPos = new HashMap<>();
    private int tickCounter = 0;

    @Override
    protected void enabled() {
        super.enabled();
        trails.clear();
        lastPos.clear();
        tickCounter = 0;
    }

    @Override
    protected void disabled() {
        super.disabled();
        trails.clear();
        lastPos.clear();
    }

    @EventListener
    public void onTickPre(OnTick.Pre event) {
        if (MC == null || MC.level == null || MC.player == null) return;
        tickCounter++;
        if (tickCounter % Math.max(1, updateRate.get()) != 0) return;

        Vec3 selfPos = MC.player.position();
        for (Player p : MC.level.players()) {
            boolean isSelf = p == MC.player;
            if (isSelf && !self.get()) continue;
            if (!isSelf && !others.get()) continue;
            if (selfPos.distanceTo(p.position()) > renderDistance.get()) continue;

            UUID id = p.getUUID();
            Vec3 cur = p.position();
            Vec3 last = lastPos.get(id);
            if (last != null && cur.distanceTo(last) < minDistance.get()) continue;

            trails.computeIfAbsent(id, k -> new ArrayDeque<>()).addFirst(new TrailPos(cur));
            Deque<TrailPos> dq = trails.get(id);
            while (dq.size() > trailLength.get()) dq.removeLast();
            lastPos.put(id, cur);
        }

        float maxAge = maxAgeSeconds.get();
        trails.entrySet().removeIf(e -> {
            boolean present = MC.level.players().stream().anyMatch(pl -> pl.getUUID().equals(e.getKey()));
            if (!present && !e.getKey().equals(MC.player.getUUID())) return true;
            e.getValue().removeIf(tp -> tp.ageSeconds() > maxAge);
            return e.getValue().isEmpty();
        });
    }

    @EventListener
    public void onRender3D(OnRender event) {
        if (MC == null || MC.level == null || MC.player == null) return;
        RenderSystem.lineWidth(lineWidth.get());
        PoseStack.Pose pose = event.poseStack.last();

        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        for (var entry : trails.entrySet()) {
            UUID id = entry.getKey();
            boolean isSelf = id.equals(MC.player.getUUID());
            float[] c = colorFor(id, isSelf);
            float baseRadius = isSelf ? selfRadius.get() : othersRadius.get();

            for (TrailPos tp : entry.getValue()) {
                float ageMul = 1.0f;
                if (fadeWithTime.get()) {
                    float t = Math.min(tp.ageSeconds(), maxAgeSeconds.get());
                    ageMul = 1.0f - (t / maxAgeSeconds.get());
                }
                if (ageMul <= 0.01f) continue;

                float cx = (float) (tp.pos.x - camX);
                float cy = (float) (tp.pos.y - camY) + 0.05f;
                float cz = (float) (tp.pos.z - camZ);

                drawCircle(event.lines, pose, cx, cy, cz, baseRadius, circleSegments.get(), c[0], c[1], c[2], c[3] * ageMul);

                if (glow.get()) {
                    int steps = Math.max(1, glowSteps.get());
                    float width = glowWidth.get();
                    float baseA = glowAlpha.get() * ageMul;
                    float pulse = 1.0f;
                    float speed = glowPulseSpeed.get();
                    if (speed > 0.0f) {
                        double time = System.currentTimeMillis() / 1000.0 * speed;
                        pulse = 0.75f + (float) (Math.sin(time * Math.PI * 2.0) * 0.25f);
                    }
                    for (int i = 1; i <= steps; i++) {
                        float f = (float) i / (float) steps;
                        float r = baseRadius + f * width;
                        float a = baseA * (1.0f - f) * pulse;
                        if (a <= 0.001f) continue;
                        drawCircle(event.lines, pose, cx, cy, cz, r, circleSegments.get(), c[0], c[1], c[2], a);
                    }
                }
            }
        }
    }

    private float[] colorFor(UUID playerId, boolean isSelf) {
        float[] base = color.get();
        if (!isSelf) {
            String idStr = playerId.toString();
            if (FriendManager.roles.containsKey(idStr)) {
                base = friendColor.get();
            }
        }
        return base;
    }

    private void drawCircle(VertexConsumer vc, PoseStack.Pose pose, float cx, float cy, float cz, float radius, int segments, float r, float g, float b, float a) {
        int segs = Math.max(8, segments);
        double step = Math.PI * 2.0 / segs;
        float prevX = cx + radius;
        float prevZ = cz;
        for (int i = 1; i <= segs; i++) {
            double ang = i * step;
            float x = (float) (cx + Math.cos(ang) * radius);
            float z = (float) (cz + Math.sin(ang) * radius);
            Render3DUtil.addLine(vc, pose, prevX, cy, prevZ, x, cy, z, r, g, b, a);
            prevX = x;
            prevZ = z;
        }
    }
}