package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.rendering.PsyopRenderTypes;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ParticleEngine extends Module {
    public IntSetting perTick = new IntSetting.Builder()
            .name("per-tick")
            .description("How many particles to spawn per tick.")
            .defaultTo(10)
            .range(1, 100)
            .addTo(coreGroup);
    public BoolSetting drawSpheres = new BoolSetting.Builder().name("spheres").defaultTo(true).addTo(coreGroup);
    public BoolSetting drawHearts = new BoolSetting.Builder().name("hearts").defaultTo(true).addTo(coreGroup);


    public ArrayList<Particle> particles = new ArrayList<>();

    public ParticleEngine() {
        super(Categories.RENDER, "particle-engine", "A custom particle engine for testing new renders.");
    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        for (int i = 0; i < perTick.get(); i++) {
            float x = Psyop.RANDOM.nextFloat() * 2 - 1;
            float y = Psyop.RANDOM.nextFloat() * 2 - 1;
            float z = Psyop.RANDOM.nextFloat() * 2 - 1;
            float size = Psyop.RANDOM.nextFloat() * 0.5f + 0.5f;
            float speed = Psyop.RANDOM.nextFloat() * 0.5f + 0.5f;
            float r = Psyop.RANDOM.nextFloat();
            float g = Psyop.RANDOM.nextFloat();
            float b = Psyop.RANDOM.nextFloat();
            float a = Psyop.RANDOM.nextFloat();
            float[] axis = {x, y, z};
            float[] rgba = {r, g, b, a};
            particles.add(new Particle(x, y, z, size, speed, rgba, axis));
        }

        if (particles.isEmpty()) return;

        List<Particle> toRemove = new ArrayList<>();

        for (Particle particle : particles) {
            particle.update();

            if (particle.markForRemoval) {
                toRemove.add(particle);
            }
        }

        particles.removeAll(toRemove);
    }

    @EventListener
    public void onRender(OnRender event) {
        if (particles.isEmpty()) return;

        var buffers = MC.renderBuffers().bufferSource();
        VertexConsumer quads = buffers.getBuffer(PsyopRenderTypes.seeThroughQuads());
        PoseStack poseStack = new PoseStack();
        PoseStack.Pose pose = poseStack.last();

        for (Particle particle : particles) {
            particle.render(quads, pose);
        }

        buffers.endBatch(PsyopRenderTypes.seeThroughQuads());
    }

    public class Particle {
        private float x, y, z;
        private final float size;
        private final float speed;
        private final float[] rgba;
        private final float[] axis;
        private int shape;
        private boolean markForRemoval = false;
        private int lifetime = 0;
        private static final BlockPos.MutableBlockPos COLLISION_CHECK_POS = new BlockPos.MutableBlockPos();

        public Particle(float x, float y, float z, float size, float speed, float[] rgba, float[] axis) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
            this.speed = speed;
            this.rgba = rgba;
            this.axis = axis;

            if (this.axis.length != 3) throw new IllegalArgumentException("Must have 3 axis values for a particle.");

            if (this.axis[1] > 0) {
                this.axis[1] = -this.axis[1];
            }

            shape = Psyop.RANDOM.nextInt(2);

            if (shape == 0 && !drawSpheres.get()) {
                shape = 1;
            }

            if (shape == 1 && !drawHearts.get()) {
                shape = 0;
            }
        }

        public void update() {
            if (MC.level == null) return;

            if (markForRemoval) {
                return;
            }

            lifetime++;

            if (lifetime >= 240) {
                markForRemoval = true;
                return;
            }

            x += ((speed * Psyop.RANDOM.nextFloat()) / 100) * axis[0];
            y += ((speed * Psyop.RANDOM.nextFloat()) / 100) * axis[1];
            z += ((speed * Psyop.RANDOM.nextFloat()) / 100) * axis[2];

            COLLISION_CHECK_POS.set((int) x + MC.player.position().x, (int) y + MC.player.position().y, (int) z + MC.player.position().z);

            if (MC.level.getBlockState(COLLISION_CHECK_POS).isSolidRender()) {
                markForRemoval = true;
            }
        }

        public void render(VertexConsumer vc, PoseStack.Pose pose) {
            if (MC.level == null) return;

            if (markForRemoval) {
                return;
            }
            
            if (shape == 0) {
                Render3DUtil.drawSphere(vc, pose, x, y, z, size / 12, 24, rgba[0], rgba[1], rgba[2], rgba[3]);
            } else if (shape == 1) {
                Render3DUtil.drawHeartEdgesXY(vc, pose, x, y, z, size / 12, 64, rgba[0], rgba[1], rgba[2], rgba[3]);
            }
        }
    }
}
