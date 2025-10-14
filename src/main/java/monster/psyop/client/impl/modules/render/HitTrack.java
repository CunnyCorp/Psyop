package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnRender;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class HitTrack extends Module {
    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{1.0f, 0.0f, 0.0f, 1.0f})
            .addTo(coreGroup);
    public BoolSetting pretty = new BoolSetting.Builder()
            .name("pretty")
            .defaultTo(false)
            .addTo(coreGroup);
    public IntSetting expireTime = new IntSetting.Builder()
            .name("expire-time")
            .defaultTo(30000)
            .range(100, 60000)
            .addTo(coreGroup);

    public FloatSetting yOffset = new FloatSetting.Builder()
            .name("y-offset")
            .defaultTo(0.4f)
            .range(-1.0f, 1.0f)
            .addTo(coreGroup);
    public FloatSetting radius = new FloatSetting.Builder()
            .name("radius")
            .defaultTo(0.6f)
            .range(0.2f, 1.0f)
            .addTo(coreGroup);
    public IntSetting segments = new IntSetting.Builder()
            .name("segments")
            .defaultTo(24)
            .range(16, 48)
            .addTo(coreGroup);

    public boolean goUp = true;
    public float r = 0;
    public List<HitPoint> hitPoints = new ArrayList<>();

    public HitTrack() {
        super(Categories.RENDER, "hit-track", "Renders a circle around an entity you hit.");
    }

    @Override
    public void update() {
        if (pretty.get()) {
            if (goUp) r += 0.01f;
            else r -= 0.01f;

            if (r >= 1.0f) {
                goUp = false;
            } else if (r <= 0.0f) {
                goUp = true;
            }
        }
    }

    @EventListener
    public void onPacket(OnPacket.Sent event) {
        if (event.packet() instanceof ServerboundInteractPacket packet) {
            if (packet.action.getType() == ServerboundInteractPacket.ActionType.ATTACK) {
                hitPoints.add(new HitPoint(MC.level.getEntity(packet.entityId).position().add(0, yOffset.get(), 0), System.currentTimeMillis(), expireTime.get()));
            }
        }
    }

    @EventListener
    public void onRender(OnRender event) {
        PoseStack.Pose pose = event.poseStack.last();

        for (HitPoint hp : hitPoints) {
            if (hp.isExpired()) {
                continue;
            }

            Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
            double camX = cam.x();
            double camY = cam.y();
            double camZ = cam.z();

            float cx = (float) (hp.pos().x - camX);
            float cy = (float) (hp.pos().y - camY);
            float cz = (float) (hp.pos().z - camZ);

            float[] c;

            if (pretty.get()) {
                c = new float[]{r, 1.0f, 1.0f, 1.0f};
            } else {
                c = color.get();
            }

            Render3DUtil.drawCircleEdgesXZ(event.quads, pose,
                    cx, cy, cz,
                    radius.get(), segments.get(),
                    c[0], c[1], c[2], c[3]);

        }
    }

    public record HitPoint(Vec3 pos, long spawnedMs, long lifetimeMs) {
        public boolean isExpired() {
            return System.currentTimeMillis() - spawnedMs > lifetimeMs;
        }
    }
}
