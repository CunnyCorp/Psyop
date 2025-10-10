package monster.psyop.client.impl.modules.hud;

import imgui.ImDrawList;
import imgui.ImGui;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.gui.GradientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class MapHUD extends HUD {
    public ColorSetting textColor =
            new ColorSetting.Builder()
                    .name("text-color")
                    .defaultTo(new float[]{0.90f, 0.90f, 0.95f, 0.95f})
                    .addTo(coreGroup);
    public ColorSetting upperColor =
            new ColorSetting.Builder()
                    .name("upper-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public ColorSetting middleColor =
            new ColorSetting.Builder()
                    .name("middle-color")
                    .defaultTo(new float[]{0.00f, 0.60f, 0.60f, 1.0f})
                    .addTo(coreGroup);
    public ColorSetting lowerColor =
            new ColorSetting.Builder()
                    .name("lower-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public final IntSetting alpha =
            new IntSetting.Builder()
                    .name("alpha")
                    .range(40, 200)
                    .defaultTo(184)
                    .addTo(coreGroup);
    public final IntSetting waveSpeed =
            new IntSetting.Builder()
                    .name("wave-speed")
                    .range(1, 10)
                    .defaultTo(3)
                    .addTo(coreGroup);
    public final IntSetting waveDensity =
            new IntSetting.Builder()
                    .name("wave-density")
                    .range(1, 10)
                    .defaultTo(5)
                    .addTo(coreGroup);

    // Map image and texture handle
    public BufferedImage image;
    public static int textureId = 0;

    private final GradientUtils gradientUtils = new GradientUtils(0.5f);

    // Caching/state
    private static final int SIZE = 128;
    private static final int RADIUS = SIZE / 2; // 64
    private int lastBlockX = Integer.MIN_VALUE;
    private int lastBlockZ = Integer.MIN_VALUE;
    private boolean initialized = false;
    private boolean dirty = false;
    private final int[][] colorCache = new int[SIZE][SIZE];

    public MapHUD() {
        super("map", "Shows a cute lil minimap.");
        image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public void uploadMapImage(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();

            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);

            ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();

            if (textureId == 0) {
                textureId = GL11.glGenTextures();
                Psyop.log("Generated new texture ID for map: {}", textureId);
            }

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            Psyop.log("Successfully uploaded map image to texture ID: {}", textureId);
        } catch (Exception e) {
            Psyop.LOG.error("Failed to upload map image to pipeline", e);
        }
    }

    private int sampleColor(int worldX, int worldZ, int playerY) {
        try {
            if (MC.level == null) return 0x00000000;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            pos.set(worldX, 0, worldZ);
            int currentHeight = MC.player.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos);
            pos.setY(currentHeight);
            int baseColor = 0x808080;
            try {
                baseColor = MC.level.getBlockState(pos).getMapColor(MC.level, pos).col;
            } catch (Exception ignored) {
            }

            pos.set(worldX, 0, worldZ - 1);
            int northHeight = MC.player.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos);
            int heightDiff = currentHeight - northHeight;
            int shaded = applyHeightGradient(baseColor, heightDiff);

            int rel = currentHeight - playerY; // positive = above player
            float relFactor = Math.max(-16f, Math.min(16f, rel)) / 16f; // clamp
            float brightAdj = 1.0f + (relFactor * 0.20f); // up to +/-20%

            int r = (shaded >> 16) & 0xFF;
            int g = (shaded >> 8) & 0xFF;
            int b = shaded & 0xFF;
            r = (int) Math.max(0, Math.min(255, r * brightAdj));
            g = (int) Math.max(0, Math.min(255, g * brightAdj));
            b = (int) Math.max(0, Math.min(255, b * brightAdj));

            return (r << 16) | (g << 8) | b;
        } catch (Throwable t) {
            return 0x000000;
        }
    }

    private void initializeCache(int centerX, int centerZ, int playerY) {
        for (int dx = -RADIUS; dx < RADIUS; dx++) {
            for (int dz = -RADIUS; dz < RADIUS; dz++) {
                int color = sampleColor(centerX + dx, centerZ + dz, playerY);
                int px = dx + RADIUS;
                int pz = dz + RADIUS;
                colorCache[px][pz] = color | 0xFF000000;
                image.setRGB(px, pz, colorCache[px][pz]);
            }
        }
        initialized = true;
        dirty = true;
    }

    private void shiftAndUpdate(int newCenterX, int newCenterZ, int playerY) {
        int dx = newCenterX - lastBlockX;
        int dz = newCenterZ - lastBlockZ;
        if (dx == 0 && dz == 0) return;

        // Shift existing cache within bounds
        int[][] tmp = new int[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int nx = x - dx;
                int nz = z - dz;
                if (nx >= 0 && nx < SIZE && nz >= 0 && nz < SIZE) {
                    tmp[x][z] = colorCache[nx][nz];
                } else {
                    tmp[x][z] = 0; // mark as empty; will fill
                }
            }
        }
        // Copy back
        for (int x = 0; x < SIZE; x++) {
            System.arraycopy(tmp[x], 0, colorCache[x], 0, SIZE);
        }

        // Fill new columns/rows exposed by movement
        for (int x = -RADIUS; x < RADIUS; x++) {
            for (int z = -RADIUS; z < RADIUS; z++) {
                int px = x + RADIUS;
                int pz = z + RADIUS;
                if (colorCache[px][pz] == 0) { // needs recompute
                    int worldX = newCenterX + x;
                    int worldZ = newCenterZ + z;
                    int color = sampleColor(worldX, worldZ, playerY) | 0xFF000000;
                    colorCache[px][pz] = color;
                }
                image.setRGB(px, pz, colorCache[px][pz]);
            }
        }
        dirty = true;
    }

    @EventListener
    public void onTickPre(OnTick.Pre event) {
        if (MC == null || MC.player == null || MC.level == null) return;

        int blockX = (int) Math.floor(MC.player.getX());
        int blockZ = (int) Math.floor(MC.player.getZ());
        int playerY = (int) Math.floor(MC.player.getY());

        if (!initialized || lastBlockX == Integer.MIN_VALUE) {
            initializeCache(blockX, blockZ, playerY);
        } else if (blockX != lastBlockX || blockZ != lastBlockZ) {
            shiftAndUpdate(blockX, blockZ, playerY);
        }

        lastBlockX = blockX;
        lastBlockZ = blockZ;

        if (dirty) {
            uploadMapImage(image);
            dirty = false;
        }
        gradientUtils.updateAnimation();
    }

    private int applyHeightGradient(int baseColor, int heightDiff) {
        float intensity = Math.max(-1.0f, Math.min(1.0f, heightDiff / 10.0f));

        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        if (intensity > 0) {
            float factor = 1.0f + (intensity * 0.4f);
            r = (int) Math.min(255, r * factor);
            g = (int) Math.min(255, g * factor);
            b = (int) Math.min(255, b * factor);
        } else if (intensity < 0) {
            float factor = 1.0f + (intensity * 0.6f);
            r = (int) Math.max(0, r * factor);
            g = (int) Math.max(0, g * factor);
            b = (int) Math.max(0, b * factor);
        }

        return (r << 16) | (g << 8) | b;
    }

    @EventListener
    public void render(On2DRender event) {
        if (textureId == 0) return; // 0 is invalid texture

        ImDrawList drawList = ImGui.getBackgroundDrawList();
        // Background panel
        drawList.addRectFilled(
                xPos.get() - 2, yPos.get() - 2,
                xPos.get() + SIZE + 2, yPos.get() + SIZE + 2,
                0x80000000
        );

        // Map image
        drawList.addImage(
                textureId,
                xPos.get(), yPos.get(),
                xPos.get() + SIZE, yPos.get() + SIZE,
                0, 0, 1, 1
        );

        // Player direction indicator (triangle at center)
        if (MC != null && MC.player != null) {
            float cx = xPos.get() + SIZE / 2.0f;
            float cy = yPos.get() + SIZE / 2.0f;
            float yawRad = (float) Math.toRadians(-MC.player.getYRot());
            float len = 5.0f;
            float back = 3.0f;
            float x1 = cx + (float) Math.cos(yawRad) * len;
            float y1 = cy + (float) Math.sin(yawRad) * len;
            float x2 = cx + (float) Math.cos(yawRad + 2.5f) * back;
            float y2 = cy + (float) Math.sin(yawRad + 2.5f) * back;
            float x3 = cx + (float) Math.cos(yawRad - 2.5f) * back;
            float y3 = cy + (float) Math.sin(yawRad - 2.5f) * back;
            int col = 0xFFFFFFFF;
            drawList.addTriangleFilled(x1, y1, x2, y2, x3, y3, col);
            drawList.addTriangle(x1, y1, x2, y2, x3, y3, 0xFF000000);
        }

        // Border
        drawList.addRect(
                xPos.get() - 1, yPos.get() - 1,
                xPos.get() + SIZE + 1, yPos.get() + SIZE + 1,
                0xFFFFFFFF
        );
    }
}