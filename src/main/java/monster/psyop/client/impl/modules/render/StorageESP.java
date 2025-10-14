package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BlockEntityColorListSetting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class StorageESP extends Module {
    public GroupedSettings targets = addGroup(new GroupedSettings("targets", "Which storage blocks to draw boxes around"));
    public BlockEntityColorListSetting storageBlockEntities = new BlockEntityColorListSetting.Builder()
            .name("storage")
            .defaultTo(List.of(
                    BlockEntityType.CHEST,
                    BlockEntityType.TRAPPED_CHEST,
                    BlockEntityType.ENDER_CHEST,
                    BlockEntityType.BARREL,
                    BlockEntityType.SHULKER_BOX
            ))
            .addTo(targets);

    public GroupedSettings style = addGroup(new GroupedSettings("style", "Style settings"));
    public BoolSetting mode2D = new BoolSetting.Builder()
            .name("2d-mode")
            .description("Render 2D billboarded rectangles instead of 3D boxes")
            .defaultTo(false)
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
            .defaultTo(0.25f)
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
            .description("How far the glow extends outwards from the box/rectangle")
            .defaultTo(0.25f)
            .range(0.05f, 0.8f)
            .addTo(style);
    public IntSetting glowSteps = new IntSetting.Builder()
            .name("glow-steps")
            .description("How many layers to draw for the glow")
            .defaultTo(3)
            .range(1, 12)
            .addTo(style);
    public FloatSetting glowAlpha = new FloatSetting.Builder()
            .name("glow-alpha")
            .description("Base alpha/intensity of the glow")
            .defaultTo(0.3f)
            .range(0.0f, 1.0f)
            .addTo(style);
    public FloatSetting glowPulseSpeed = new FloatSetting.Builder()
            .name("glow-pulse-speed")
            .description("Speed of glow pulsing (0 to disable pulsing)")
            .defaultTo(1.0f)
            .range(0.0f, 5.0f)
            .addTo(style);

    public StorageESP() {
        super(Categories.RENDER, "storage-esp", "Draws 3D boxes around storage blocks.");
    }

    private boolean isNotTarget(BlockEntity be) {
        BlockEntityType<?> t = be.getType();
        return !storageBlockEntities.value().contains(t);
    }

    private float[] colorFor(BlockEntity be) {
        float[] base = storageBlockEntities.colorMap.getOrDefault(
                be.getType(),
                new float[]{1.0f, 0.6f, 0.0f, 1.0f}
        );
        if (rainbow.get()) {
            float hue = (float) ((System.currentTimeMillis() / 1000.0) * rainbowSpeed.get());
            hue = hue - (float) Math.floor(hue);
            float[] rgb = Render3DUtil.hsbToRgb(hue, 0.8f, 1.0f);
            return new float[]{rgb[0], rgb[1], rgb[2], base[3]};
        }
        return base;
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

        int rd = MC.options.renderDistance().get() + 2;
        Set<BlockPos> skipped = new HashSet<>();
        for (int dx = -rd; dx <= rd; dx++) {
            for (int dz = -rd; dz <= rd; dz++) {
                int cx = MC.player.chunkPosition().x + dx;
                int cz = MC.player.chunkPosition().z + dz;
                if (!MC.level.hasChunk(cx, cz)) continue;
                var chunk = MC.level.getChunk(cx, cz);
                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    if (skipped.contains(pos)) continue;
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be == null) continue;
                    if (isNotTarget(be)) continue;

                    skipped.add(pos);
                    ArrayDeque<BlockPos> dq = new ArrayDeque<>();
                    dq.add(pos);

                    Set<BlockPos> group = new HashSet<>();
                    group.add(pos);

                    while (!dq.isEmpty()) {
                        BlockPos p = dq.pollFirst();
                        int px = p.getX();
                        int py = p.getY();
                        int pz = p.getZ();
                        BlockPos[] neighbors = new BlockPos[]{
                                new BlockPos(px + 1, py, pz),
                                new BlockPos(px - 1, py, pz),
                                new BlockPos(px, py + 1, pz),
                                new BlockPos(px, py - 1, pz),
                                new BlockPos(px, py, pz + 1),
                                new BlockPos(px, py, pz - 1)
                        };
                        for (BlockPos np : neighbors) {
                            if (skipped.contains(np)) continue;
                            BlockEntity nbe = MC.level.getBlockEntity(np);
                            if (nbe == null) continue;
                            if (nbe.getType() != be.getType()) continue;
                            skipped.add(np);
                            dq.add(np);
                            group.add(np);
                        }
                    }

                    List<int[]> prisms = new ArrayList<>();
                    Set<BlockPos> remaining = new HashSet<>(group);
                    while (!remaining.isEmpty()) {
                        BlockPos start = remaining.iterator().next();
                        int x0 = start.getX();
                        int y0 = start.getY();
                        int z0 = start.getZ();

                        int x1 = x0;
                        while (remaining.contains(new BlockPos(x1 + 1, y0, z0))) x1++;

                        int z1 = z0;
                        outerZ:
                        while (true) {
                            int nz = z1 + 1;
                            for (int x = x0; x <= x1; x++) {
                                if (!remaining.contains(new BlockPos(x, y0, nz))) {
                                    break outerZ;
                                }
                            }
                            z1 = nz;
                        }

                        int y1 = y0;
                        outerY:
                        while (true) {
                            int ny = y1 + 1;
                            for (int x = x0; x <= x1; x++) {
                                for (int z = z0; z <= z1; z++) {
                                    if (!remaining.contains(new BlockPos(x, ny, z))) {
                                        break outerY;
                                    }
                                }
                            }
                            y1 = ny;
                        }

                        for (int y = y0; y <= y1; y++) {
                            for (int x = x0; x <= x1; x++) {
                                for (int z = z0; z <= z1; z++) {
                                    remaining.remove(new BlockPos(x, y, z));
                                }
                            }
                        }

                        prisms.add(new int[]{x0, y0, z0, x1 + 1, y1 + 1, z1 + 1});
                    }

                    float[] c = colorFor(be);

                    for (int[] b : prisms) {
                        double minX = b[0] - camX;
                        double minY = b[1] - camY;
                        double minZ = b[2] - camZ;
                        double maxX = b[3] - camX;
                        double maxY = b[4] - camY;
                        double maxZ = b[5] - camZ;

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
                            float cPX = (float) ((minX + maxX) * 0.5);
                            float cPY = (float) ((minY + maxY) * 0.5);
                            float cPZ = (float) ((minZ + maxZ) * 0.5);
                            float halfW = (float) (Math.max(maxX - minX, maxZ - minZ) * 0.5);
                            float halfH = (float) ((maxY - minY) * 0.5);
                            float rX = -leftV.x();
                            float rY = -leftV.y();
                            float rZ = -leftV.z();
                            float uX = upV.x();
                            float uY = upV.y();
                            float uZ = upV.z();

                            if (filled.get()) {
                                float a = Math.max(0.0f, Math.min(1.0f, fillAlpha.get()));
                                Render3DUtil.drawBillboardQuadFaces(
                                        event.quads, pose,
                                        cPX, cPY, cPZ,
                                        rX, rY, rZ,
                                        uX, uY, uZ,
                                        halfW, halfH,
                                        c[0], c[1], c[2], a);
                            }

                            Render3DUtil.drawBillboardCornerQuads(
                                    event.quads, pose,
                                    cPX, cPY, cPZ,
                                    rX, rY, rZ,
                                    uX, uY, uZ,
                                    halfW, halfH,
                                    0.33f, 0.02f,
                                    c[0], c[1], c[2], c[3]);

                            if (glow.get()) {
                                int steps = Math.max(1, glowSteps.get());
                                float width = glowWidth.get();
                                float baseA = glowAlpha.get();
                                float pulseSpeed = glowPulseSpeed.get();
                                float pulse = 1.0f;
                                if (pulseSpeed > 0.0f) {
                                    double tPulse = System.currentTimeMillis() / 1000.0 * pulseSpeed;
                                    pulse = 0.75f + (float) (Math.sin(tPulse * Math.PI * 2.0) * 0.25f);
                                }
                                for (int i = 1; i <= steps; i++) {
                                    float f = (float) i / (float) steps;
                                    float expand = f * width;
                                    float a = baseA * (1.0f - f) * pulse;
                                    if (a <= 0.001f) continue;
                                    Render3DUtil.drawBillboardCornerQuads(
                                            event.quads, pose,
                                            cPX, cPY, cPZ,
                                            rX, rY, rZ,
                                            uX, uY, uZ,
                                            halfW + expand, halfH + expand,
                                            0.33f, 0.02f,
                                            c[0], c[1], c[2], a);
                                }
                            }
                        } else {
                            if (filled.get()) {
                                float a = Math.max(0.0f, Math.min(1.0f, fillAlpha.get()));
                                Render3DUtil.drawBoxFaces(event.quads, pose,
                                        (float) minX, (float) minY, (float) minZ,
                                        (float) maxX, (float) maxY, (float) maxZ,
                                        c[0], c[1], c[2], a);
                            }

                            Render3DUtil.drawBoxEdges(event.lines, pose,
                                    (float) minX, (float) minY, (float) minZ,
                                    (float) maxX, (float) maxY, (float) maxZ,
                                    c[0], c[1], c[2], c[3]);

                            if (glow.get()) {
                                int steps = Math.max(1, glowSteps.get());
                                float width = glowWidth.get();
                                float baseA = glowAlpha.get();
                                float pulseSpeed = glowPulseSpeed.get();
                                float pulse = 1.0f;
                                if (pulseSpeed > 0.0f) {
                                    double tPulse = System.currentTimeMillis() / 1000.0 * pulseSpeed;
                                    pulse = 0.75f + (float) (Math.sin(tPulse * Math.PI * 2.0) * 0.25f);
                                }
                                for (int i = 1; i <= steps; i++) {
                                    float f = (float) i / (float) steps;
                                    float expand = f * width;
                                    float a = baseA * (1.0f - f) * pulse;
                                    if (a <= 0.001f) continue;
                                    Render3DUtil.drawBoxFaces(
                                            event.quads, pose,
                                            (float) (minX - expand), (float) (minY - expand), (float) (minZ - expand),
                                            (float) (maxX + expand), (float) (maxY + expand), (float) (maxZ + expand),
                                            c[0], c[1], c[2], a);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
