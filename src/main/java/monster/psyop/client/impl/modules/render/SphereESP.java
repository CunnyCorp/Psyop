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
import monster.psyop.client.impl.events.game.OnRender;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;

public class SphereESP extends Module {
    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{1.0f, 0.0f, 0.0f, 1.0f})
            .addTo(coreGroup);
    public ColorSetting friendColor = new ColorSetting.Builder()
            .name("friend-color")
            .defaultTo(new float[]{0.0f, 1.0f, 0.0f, 1.0f})
            .addTo(coreGroup);
    public BoolSetting pretty = new BoolSetting.Builder()
            .name("pretty")
            .defaultTo(false)
            .addTo(coreGroup);
    public FloatSetting fillOpacity = new FloatSetting.Builder()
            .name("fill-opacity")
            .defaultTo(0.5f)
            .range(0.1f, 1.0f)
            .addTo(coreGroup);
    public FloatSetting radius = new FloatSetting.Builder()
            .name("radius")
            .defaultTo(1.4f)
            .range(0.1f, 3.0f)
            .addTo(coreGroup);
    public IntSetting segments = new IntSetting.Builder()
            .name("segments")
            .defaultTo(24)
            .range(16, 32)
            .addTo(coreGroup);

    public boolean goUp = true;
    public float r = 0;

    public SphereESP() {
        super(Categories.RENDER, "sphere-esp", "My doctor told me I was spherically gifted.");
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
    public void onRender(OnRender event) {
        PoseStack.Pose pose = event.poseStack.last();

        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        for (AbstractClientPlayer player : MC.level.players()) {
            if (player == MC.player) continue;

            float cx = (float) (player.position().x - camX);
            float cy = (float) (player.position().y - camY) + player.getEyeHeight() / 2;
            float cz = (float) (player.position().z - camZ);

            float[] c;

            if (pretty.get()) {
                c = new float[]{r, 1.0f, 1.0f, fillOpacity.get()};
            } else if (FriendManager.canAttack(player)) {
                c = new float[]{color.get()[0], color.get()[1], color.get()[2], fillOpacity.get()};
            } else {
                c = new float[]{friendColor.get()[0], friendColor.get()[1], friendColor.get()[2], fillOpacity.get()};
            }

            Render3DUtil.drawSphereFaces(event.quads, pose, cx, cy, cz, radius.get(), segments.get(), c[0], c[1], c[2], c[3]);
        }
    }
}
