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
    public BufferedImage image;
    public static int textureId = 0;

    private final GradientUtils gradientUtils = new GradientUtils(0.5f);

    public MapHUD() {
        super("map", "Shows a cute lil minimap.");
        image = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
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


    @EventListener
    public void onTickPre(OnTick.Pre event) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int x = -64; x < 64; x++) {
            for (int z = -64; z < 64; z++) {
                mutableBlockPos.set(MC.player.getX() + x, 0, MC.player.getZ() + z);
                int currentHeight = MC.player.level().getHeight(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos);
                mutableBlockPos.setY(currentHeight);
                int baseColor = MC.level.getBlockState(mutableBlockPos).getMapColor(MC.level, mutableBlockPos).col;
                mutableBlockPos.set(MC.player.getX() + x, 0, MC.player.getZ() + z - 1);
                int northHeight = MC.player.level().getHeight(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos);
                int heightDiff = currentHeight - northHeight;
                int gradientColor = applyHeightGradient(baseColor, heightDiff);
                image.setRGB(x + 64, z + 64, gradientColor | 0xFF000000);
            }
        }

        uploadMapImage(image);
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
        if (textureId == -1) return;

        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addRectFilled(
                xPos.get() - 2, yPos.get() - 2,
                xPos.get() + 130, yPos.get() + 130,
                0x80000000
        );

        drawList.addImage(
                textureId,
                xPos.get(), yPos.get(),
                xPos.get() + 128, yPos.get() + 128,
                0, 0, 1, 1
        );

        drawList.addRect(
                xPos.get() - 1, yPos.get() - 1,
                xPos.get() + 129, yPos.get() + 129,
                0xFFFFFFFF
        );
    }
}