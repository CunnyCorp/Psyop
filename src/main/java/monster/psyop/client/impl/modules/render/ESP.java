package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.framework.rendering.PsyopRenderTypes;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ESP extends Module {
    public GroupedSettings targets = addGroup(new GroupedSettings("targets", "Which entities to draw boxes around"));
    public BoolSetting players = new BoolSetting.Builder()
            .name("players")
            .defaultTo(true)
            .addTo(targets);
    public BoolSetting hostiles = new BoolSetting.Builder()
            .name("hostiles")
            .defaultTo(true)
            .addTo(targets);
    public BoolSetting animals = new BoolSetting.Builder()
            .name("animals")
            .defaultTo(false)
            .addTo(targets);
    public BoolSetting invisibles = new BoolSetting.Builder()
            .name("invisibles")
            .defaultTo(false)
            .addTo(targets);
    public BoolSetting friendsOnly = new BoolSetting.Builder()
            .name("friends-only")
            .defaultTo(false)
            .addTo(targets);
    public EntityColorListSetting colors = new EntityColorListSetting.Builder()
            .name("colors")
            .defaultTo(List.of())
            .addTo(targets);

    public GroupedSettings style = addGroup(new GroupedSettings("style", "Style settings"));
    public BoolSetting mode2D = new BoolSetting.Builder()
            .name("2d-mode")
            .description("Render 2D billboarded rectangles instead of 3D boxes")
            .defaultTo(false)
            .addTo(style);
    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{0.0f, 1.0f, 0.0f, 1.0f})
            .addTo(style);
    public ColorSetting friendColor = new ColorSetting.Builder()
            .name("friend-color")
            .defaultTo(new float[]{0.0f, 0.6f, 1.0f, 1.0f})
            .addTo(style);
    public FloatSetting lineWidth = new FloatSetting.Builder()
            .name("line-width")
            .defaultTo(2.0f)
            .range(1.0f, 6.0f)
            .addTo(style);
    public FloatSetting padding = new FloatSetting.Builder()
            .name("padding")
            .description("Expands or shrinks the ESP box size")
            .defaultTo(0.0f)
            .range(-0.5f, 1.0f)
            .addTo(style);
    public BoolSetting filled = new BoolSetting.Builder()
            .name("filled")
            .defaultTo(false)
            .addTo(style);
    public FloatSetting fillAlpha = new FloatSetting.Builder()
            .name("fill-alpha")
            .defaultTo(0.3f)
            .range(0.0f, 1.0f)
            .addTo(style);
    public BoolSetting rainbow = new BoolSetting.Builder()
            .name("rainbow")
            .defaultTo(false)
            .addTo(style);
    public FloatSetting rainbowSpeed = new FloatSetting.Builder()
            .name("rainbow-speed")
            .defaultTo(0.2f)
            .range(0.01f, 5.0f)
            .addTo(style);

    public BoolSetting healthbar2D = new BoolSetting.Builder()
            .name("2d-healthbar")
            .description("Render a vertical health bar next to 2D billboard boxes")
            .defaultTo(true)
            .addTo(style);

    public BoolSetting glow = new BoolSetting.Builder()
            .name("glow")
            .defaultTo(true)
            .addTo(style);
    public BoolSetting hideLines = new BoolSetting.Builder()
            .name("hide-lines")
            .description("Hide back-facing lines (depth-tested lines instead of see-through)")
            .defaultTo(true)
            .addTo(style);
    public FloatSetting glowWidth = new FloatSetting.Builder()
            .name("glow-width")
            .description("How far the glow extends outwards from the box")
            .defaultTo(0.35f)
            .range(0.05f, 0.8f)
            .addTo(style);
    public IntSetting glowSteps = new IntSetting.Builder()
            .name("glow-steps")
            .description("How many layers to draw for the glow")
            .defaultTo(4)
            .range(1, 12)
            .addTo(style);
    public FloatSetting glowAlpha = new FloatSetting.Builder()
            .name("glow-alpha")
            .description("Base alpha/intensity of the glow")
            .defaultTo(0.35f)
            .range(0.0f, 1.0f)
            .addTo(style);
    public FloatSetting glowPulseSpeed = new FloatSetting.Builder()
            .name("glow-pulse-speed")
            .description("Speed of glow pulsing (0 to disable pulsing)")
            .defaultTo(1.0f)
            .range(0.0f, 5.0f)
            .addTo(style);

    public ESP() {
        super(Categories.RENDER, "box-esp", "Draws 3D boxes around entities.");
    }

    public boolean shouldRender(Entity e) {
        if (!(e instanceof LivingEntity)) return false;
        if (e == MC.player) return false;
        if (e instanceof Player) {
            if (!players.get()) return false;
            if (friendsOnly.get()) {
                String uuidKey = e.getUUID().toString();
                String nameKey = e.getName().getString();
                return FriendManager.roles.containsKey(uuidKey) || FriendManager.roles.containsKey(nameKey);
            }
            return true;
        }
        if (e.getType().getCategory() == MobCategory.MONSTER || e instanceof Monster) {
            return hostiles.get();
        }
        if (e.getType().getCategory() == MobCategory.CREATURE || e.getType().getCategory() == MobCategory.AMBIENT || e.getType().getCategory() == MobCategory.WATER_CREATURE) {
            return animals.get();
        }
        return false;
    }

    public float[] colorFor(Entity e) {
        float[] base = colors.colorMap.getOrDefault(e.getType(), color.get());
        if (e instanceof Player) {
            String uuidKey = e.getUUID().toString();
            String nameKey = e.getName().getString();
            if (FriendManager.roles.containsKey(uuidKey) || FriendManager.roles.containsKey(nameKey)) {
                base = friendColor.get();
            }
        }
        if (rainbow.get()) {
            float hue = (float) ((System.currentTimeMillis() / 1000.0) * rainbowSpeed.get());
            hue = hue - (float) Math.floor(hue);
            float[] rgb = Render3DUtil.hsbToRgb(hue, 0.8f, 1.0f);
            return new float[]{rgb[0], rgb[1], rgb[2], base[3]};
        }
        return base;
    }

    public boolean allowInvisible(Entity e) {
        if (!invisibles.get() && e.isInvisible()) return false;
        return true;
    }

    @EventListener
    public void onRender3D(OnRender event) {
        if (MC == null || MC.level == null || MC.player == null) return;

        Iterable<Entity> entities = MC.level.entitiesForRendering();

        RenderSystem.lineWidth(lineWidth.get());
        var buffers = MC.renderBuffers().bufferSource();
        VertexConsumer lines = hideLines.get() ? null : buffers.getBuffer(PsyopRenderTypes.seeThroughLines());
        boolean needQuads = mode2D.get() || filled.get() || glow.get();
        VertexConsumer quads = needQuads ? buffers.getBuffer(PsyopRenderTypes.seeThroughQuads()) : null;
        PoseStack poseStack = new PoseStack();
        PoseStack.Pose pose = poseStack.last();

        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        for (Entity e : entities) {
            if (!shouldRender(e)) continue;
            if (!allowInvisible(e)) continue;

            float[] c = colorFor(e);
            AABB bb = e.getBoundingBox();
            float minX = (float) (bb.minX - camX);
            float minY = (float) (bb.minY - camY);
            float minZ = (float) (bb.minZ - camZ);
            float maxX = (float) (bb.maxX - camX);
            float maxY = (float) (bb.maxY - camY);
            float maxZ = (float) (bb.maxZ - camZ);

            // Apply configurable padding to the 3D bounds
            float pad = padding.get();
            if (pad != 0.0f) {
                minX -= pad;
                minY -= pad;
                minZ -= pad;
                maxX += pad;
                maxY += pad;
                maxZ += pad;
            }

            if (mode2D.get()) {
                var leftV = MC.gameRenderer.getMainCamera().getLeftVector();
                var upV = MC.gameRenderer.getMainCamera().getUpVector();
                float cPX = (minX + maxX) * 0.5f;
                float cPY = (minY + maxY) * 0.5f;
                float cPZ = (minZ + maxZ) * 0.5f;
                float halfW = Math.max(maxX - minX, maxZ - minZ) * 0.5f;
                float halfH = (maxY - minY) * 0.5f;
                float rX = -leftV.x();
                float rY = -leftV.y();
                float rZ = -leftV.z();
                float uX = upV.x();
                float uY = upV.y();
                float uZ = upV.z();

                if (quads != null && filled.get()) {
                    float a = Math.max(0.0f, Math.min(1.0f, fillAlpha.get()));
                    Render3DUtil.drawBillboardQuadFaces(
                            quads, pose,
                            cPX, cPY, cPZ,
                            rX, rY, rZ,
                            uX, uY, uZ,
                            halfW, halfH,
                            c[0], c[1], c[2], a);
                }
                if (quads != null) {
                    Render3DUtil.drawBillboardCornerQuads(
                            quads, pose,
                            cPX, cPY, cPZ,
                            rX, rY, rZ,
                            uX, uY, uZ,
                            halfW, halfH,
                            0.33f, 0.02f,
                            c[0], c[1], c[2], c[3]);
                }

                // 2D healthbar rendering
                if (healthbar2D.get() && quads != null && e instanceof LivingEntity le) {
                    float maxHp = le.getMaxHealth();
                    float curHp = le.getHealth();
                    if (maxHp > 0.0f) {
                        float ratio = Math.max(0.0f, Math.min(1.0f, curHp / maxHp));

                        // Bar dimensions and placement: to the right of the box
                        float margin = 0.05f;
                        float barHalfW = 0.02f;
                        float barHalfH = halfH;

                        // Right side center offset from cP
                        float offRight = halfW + margin + barHalfW;
                        float barCX = cPX + rX * offRight;
                        float barCY = cPY + rY * offRight;
                        float barCZ = cPZ + rZ * offRight;

                        // Background (dark)
                        Render3DUtil.drawBillboardQuadFaces(
                                quads, pose,
                                barCX, barCY, barCZ,
                                rX, rY, rZ,
                                uX, uY, uZ,
                                barHalfW, barHalfH,
                                0.05f, 0.05f, 0.05f, 0.45f);

                        // Foreground (filled portion from bottom to top)
                        float fillHalfH = barHalfH * ratio;
                        float upOffset = -barHalfH + fillHalfH; // move to center of filled segment
                        float fillCX = barCX + uX * upOffset;
                        float fillCY = barCY + uY * upOffset;
                        float fillCZ = barCZ + uZ * upOffset;

                        // Gradient from red (low) to green (high)
                        float g = ratio;
                        float r = 1.0f - ratio;
                        float b = 0.0f;
                        Render3DUtil.drawBillboardQuadFaces(
                                quads, pose,
                                fillCX, fillCY, fillCZ,
                                rX, rY, rZ,
                                uX, uY, uZ,
                                barHalfW, fillHalfH,
                                r, g, b, 0.95f);
                    }
                }

                if (glow.get() && quads != null) {
                    int steps = Math.max(1, glowSteps.get());
                    float width = glowWidth.get();
                    float baseA = glowAlpha.get();
                    float pulseSpeed = glowPulseSpeed.get();
                    float pulse = 1.0f;
                    if (pulseSpeed > 0.0f) {
                        double t = System.currentTimeMillis() / 1000.0 * pulseSpeed;
                        pulse = 0.75f + (float) (Math.sin(t * Math.PI * 2.0) * 0.25f);
                    }
                    for (int i = 1; i <= steps; i++) {
                        float f = (float) i / (float) steps;
                        float expand = f * width;
                        float a = baseA * (1.0f - f) * pulse;
                        if (a <= 0.001f) continue;
                        Render3DUtil.drawBillboardCornerQuads(
                                quads, pose,
                                cPX, cPY, cPZ,
                                rX, rY, rZ,
                                uX, uY, uZ,
                                halfW + expand, halfH + expand,
                                0.33f, 0.02f,
                                c[0], c[1], c[2], a);
                    }
                }
            } else {
                if (quads != null && filled.get()) {
                    float a = Math.max(0.0f, Math.min(1.0f, fillAlpha.get()));
                    Render3DUtil.drawBoxFaces(quads, pose, minX, minY, minZ, maxX, maxY, maxZ, c[0], c[1], c[2], a);
                }

                if (lines != null) {
                    Render3DUtil.drawBoxEdges(lines, pose, minX, minY, minZ, maxX, maxY, maxZ, c[0], c[1], c[2], c[3]);
                }

                if (glow.get() && quads != null) {
                    int steps = Math.max(1, glowSteps.get());
                    float width = glowWidth.get();
                    float baseA = glowAlpha.get();
                    float pulseSpeed = glowPulseSpeed.get();
                    float pulse = 1.0f;
                    if (pulseSpeed > 0.0f) {
                        double t = System.currentTimeMillis() / 1000.0 * pulseSpeed;
                        pulse = 0.75f + (float) (Math.sin(t * Math.PI * 2.0) * 0.25f);
                    }
                    for (int i = 1; i <= steps; i++) {
                        float f = (float) i / (float) steps;
                        float expand = f * width;
                        float a = baseA * (1.0f - f) * pulse;
                        if (a <= 0.001f) continue;
                        Render3DUtil.drawBoxFaces(
                                quads, pose,
                                minX - expand, minY - expand, minZ - expand,
                                maxX + expand, maxY + expand, maxZ + expand,
                                c[0], c[1], c[2], a);
                    }
                }
            }
        }

        if (quads != null) buffers.endBatch(PsyopRenderTypes.seeThroughQuads());
        if (lines != null) buffers.endBatch(PsyopRenderTypes.seeThroughLines());
    }

}
