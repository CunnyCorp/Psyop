package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnRender;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PopESP extends Module {
    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{1.0f, 0.0f, 0.0f, 1.0f})
            .addTo(coreGroup);
    public ColorSetting friendColor = new ColorSetting.Builder()
            .name("friend-color")
            .defaultTo(new float[]{0.0f, 1.0f, 0.0f, 1.0f})
            .addTo(coreGroup);
    public ColorSetting selfColor = new ColorSetting.Builder()
            .name("self-color")
            .defaultTo(new float[]{0.0f, 1.0f, 0.0f, 1.0f})
            .addTo(coreGroup);
    public BoolSetting pretty = new BoolSetting.Builder()
            .name("pretty")
            .defaultTo(false)
            .addTo(coreGroup);
    public FloatSetting fillOpacity = new FloatSetting.Builder()
            .name("fill-opacity")
            .defaultTo(0.3f)
            .range(0.1f, 1.0f)
            .addTo(coreGroup);
    public FloatSetting radius = new FloatSetting.Builder()
            .name("radius")
            .defaultTo(0.5f)
            .range(0.1f, 3.0f)
            .addTo(coreGroup);
    public IntSetting segments = new IntSetting.Builder()
            .name("segments")
            .defaultTo(24)
            .range(16, 32)
            .addTo(coreGroup);
    public IntSetting keepTime = new IntSetting.Builder()
            .name("keep-time")
            .description("The time to keep the renders in MS.")
            .range(1000, 240000)
            .defaultTo(120000)
            .addTo(coreGroup);

    public boolean goUp = true;
    public float g = 0;
    private final List<TotemPop> totemPops = new ArrayList<>();

    public PopESP() {
        super(Categories.RENDER, "pop-esp", "Render totem pops with spheres.");
    }

    @Override
    public void update() {
        if (pretty.get()) {
            if (goUp) g += 0.01f;
            else g -= 0.01f;

            if (g >= 1.0f) {
                goUp = false;
            } else if (g <= 0.0f) {
                goUp = true;
            }
        }
    }

    @EventListener
    public void onPacket(OnPacket.Received event) {
        if (event.packet() instanceof ClientboundEntityEventPacket packet) {
            if (packet.getEventId() == 35) {
                if (packet.getEntity(MC.level) instanceof AbstractClientPlayer player) {
                    if (player.getUUID().equals(MC.player.getUUID())) {
                        totemPops.add(new TotemPop(player.position(), System.currentTimeMillis(), keepTime.get(), selfColor.get()));
                    } else if (!FriendManager.canAttack(player)) {
                        totemPops.add(new TotemPop(player.position(), System.currentTimeMillis(), keepTime.get(), friendColor.get()));
                    } else {
                        totemPops.add(new TotemPop(player.position(), System.currentTimeMillis(), keepTime.get(), pretty.get() ? null : color.get()));
                    }
                }
            }
        }
    }

    @EventListener
    public void onRender(OnRender event) {
        List<TotemPop> toRemove = new ArrayList<>();

        for (TotemPop pop : totemPops) {
            if (pop.isExpired()) {
                toRemove.add(pop);
                return;
            }
        }

        totemPops.removeAll(toRemove);

        PoseStack.Pose pose = event.poseStack.last();

        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        for (TotemPop pop : totemPops) {
            float cx = (float) (pop.pos().x - camX);
            float cy = (float) (pop.pos().y - camY) + radius.get();
            float cz = (float) (pop.pos().z - camZ);

            float[] c = pop.color();

            if (c == null) c = new float[]{1.0f, g, 1.0f, fillOpacity.get()};

            Render3DUtil.drawSphereFaces(event.quads, pose, cx, cy, cz, radius.get(), segments.get(), c[0], c[1], c[2], fillOpacity.get());
        }
    }

    public record TotemPop(Vec3 pos, long time, long keepTime, float @Nullable [] color) {
        public boolean isExpired() {
            return System.currentTimeMillis() - time > keepTime;
        }
    }
}
