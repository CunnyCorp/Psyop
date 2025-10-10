package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.rendering.PsyopRenderTypes;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tracers module: draws a line from your camera to selected entities.
 */
public class Tracers extends Module {
    // Target filters (mirrors BoxESP for consistency)
    public GroupedSettings targets = addGroup(new GroupedSettings("targets", "Which entities to draw tracers to"));
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

    // Style
    public GroupedSettings style = addGroup(new GroupedSettings("style", "Style settings"));
    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 1.0f})
            .addTo(style);
    public ColorSetting friendColor = new ColorSetting.Builder()
            .name("friend-color")
            .defaultTo(new float[]{0.0f, 0.9f, 0.3f, 1.0f})
            .addTo(style);
    public FloatSetting lineWidth = new FloatSetting.Builder()
            .name("line-width")
            .defaultTo(1.5f)
            .range(0.5f, 6.0f)
            .addTo(style);
    public BoolSetting rainbow = new BoolSetting.Builder()
            .name("rainbow")
            .defaultTo(false)
            .addTo(style);
    public FloatSetting rainbowSpeed = new FloatSetting.Builder()
            .name("rainbow-speed")
            .defaultTo(0.2f) // cycles per second
            .range(0.01f, 5.0f)
            .addTo(style);

    public Tracers() {
        super(Categories.RENDER, "tracers", "Draws lines from your camera to entities.");
    }

    @EventListener
    public void onRender3D(OnRender event) {
        Minecraft mc = Psyop.MC;
        if (mc.level == null || mc.player == null) return;

        // Use the same entity source as BoxESP (no frustum cull where possible)
        Iterable<Entity> entities = mc.level.entitiesForRendering();

        // Prepare rendering state & buffer
        RenderSystem.lineWidth(lineWidth.get());
        var buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(PsyopRenderTypes.seeThroughLines());
        PoseStack poseStack = new PoseStack();
        PoseStack.Pose pose = poseStack.last();

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        for (Entity e : entities) {
            if (e == mc.player) continue;
            if (!shouldRender(e)) continue;
            if (!allowInvisible(e)) continue;

            float[] c = colorFor(e);

            AABB bb = e.getBoundingBox();
            double tx = (bb.minX + bb.maxX) * 0.5;
            double ty = (bb.minY + bb.maxY) * 0.5;
            double tz = (bb.minZ + bb.maxZ) * 0.5;

            Vec3 look = mc.player.getViewVector(1.0f);
            float x0 = (float) (look.x * 0.1);
            float y0 = (float) (look.y * 0.1);
            float z0 = (float) (look.z * 0.1);

            float x1 = (float) (tx - camX);
            float y1 = (float) (ty - camY);
            float z1 = (float) (tz - camZ);

            Render3DUtil.drawTracer(lines, pose, x0, y0, z0, x1, y1, z1, c[0], c[1], c[2], c[3]);
        }

        buffers.endBatch(PsyopRenderTypes.seeThroughLines());
    }


    public boolean shouldRender(Entity e) {
        if (!(e instanceof LivingEntity)) return false;
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
        float[] base = color.get();
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
}
