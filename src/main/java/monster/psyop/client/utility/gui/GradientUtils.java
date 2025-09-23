package monster.psyop.client.utility.gui;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImDrawFlags;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;

import java.awt.*;

public class GradientUtils {
    private long startTime;
    private float animationPhase = 0f;
    private float animationSpeed;

    public GradientUtils(float animationSpeed) {
        this.startTime = System.currentTimeMillis();
        this.animationSpeed = animationSpeed;
    }

    public GradientUtils() {
        this(0.5f);
    }

    public void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float elapsed = (currentTime - startTime) / 1000f;
        animationPhase = (elapsed * animationSpeed) % 1f;
    }

    public void drawAnimatedGradientBackground(float x, float y, float width, float height,
                                               int index, int totalItems,
                                               Color[] gradientColors, int alpha) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();

        float positionFactor = totalItems > 1 ? (float) index / (totalItems - 1) : 0.5f;
        float animatedFactor = (positionFactor + animationPhase) % 1f;

        Color gradientColor = getMultiGradientColor(gradientColors, animatedFactor, alpha);

        Color outlineColor = new Color(
                Math.max(0, gradientColor.getRed() - 40),
                Math.max(0, gradientColor.getGreen() - 40),
                Math.max(0, gradientColor.getBlue() - 40),
                255
        );

        drawList.addRectFilled(x, y, x + width, y + height,
                new ImColorW(gradientColor).packed(), 4f, ImDrawFlags.RoundCornersAll);
    }

    public void drawWaveGradientTile(float x, float y, float width, float height,
                                     Color[] gradientColors, int alpha,
                                     float waveSpeed, float waveDensity) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();

        int segments = Math.max(2, (int)(height * waveDensity));
        float segmentHeight = height / segments;

        for (int i = 0; i < segments; i++) {
            float segmentY = y + (i * segmentHeight);

            float wavePosition = (float)i / segments;
            float waveOffset = (animationPhase * waveSpeed + wavePosition) % 1f;

            Color segmentColor = getMultiGradientColor(gradientColors, waveOffset, alpha);

            drawList.addRectFilled(x, segmentY, x + width, segmentY + segmentHeight,
                    new ImColorW(segmentColor).packed(), 0f, ImDrawFlags.None);
        }

        Color borderColor = new Color(
                gradientColors[0].getRed() / 2,
                gradientColors[0].getGreen() / 2,
                gradientColors[0].getBlue() / 2,
                150
        );
        drawList.addRect(x, y, x + width, y + height,
                new ImColorW(borderColor).packed(), 4f, ImDrawFlags.RoundCornersAll, 2f);
    }

    public void drawHorizontalWaveGradientTile(float x, float y, float width, float height,
                                               Color[] gradientColors, int alpha,
                                               float waveSpeed, float waveDensity) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();

        int segments = Math.max(2, (int)(width * waveDensity));
        float segmentWidth = width / segments;

        for (int i = 0; i < segments; i++) {
            float segmentX = x + (i * segmentWidth);

            float wavePosition = (float)i / segments;
            float waveOffset = (animationPhase * waveSpeed + wavePosition) % 1f;

            Color segmentColor = getMultiGradientColor(gradientColors, waveOffset, alpha);

            drawList.addRectFilled(segmentX, y, segmentX + segmentWidth, y + height,
                    new ImColorW(segmentColor).packed(), 0f, ImDrawFlags.None);
        }

        Color borderColor = new Color(
                gradientColors[0].getRed() / 2,
                gradientColors[0].getGreen() / 2,
                gradientColors[0].getBlue() / 2,
                150
        );
        drawList.addRect(x, y, x + width, y + height,
                new ImColorW(borderColor).packed(), 4f, ImDrawFlags.RoundCornersAll, 2f);
    }

    /**
     * Draws a radial wave gradient tile (wave flows from center outward)
     *
     * @param centerX Center X position of the tile
     * @param centerY Center Y position of the tile
     * @param radius Radius of the circular tile
     * @param gradientColors Array of colors to use in the gradient wave
     * @param alpha Alpha transparency value
     * @param waveSpeed Speed of the wave animation (higher = faster)
     * @param waveDensity Density of color segments in the wave (higher = more segments)
     */
    public void drawRadialWaveGradientTile(float centerX, float centerY, float radius,
                                           Color[] gradientColors, int alpha,
                                           float waveSpeed, float waveDensity) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();

        int segments = Math.max(8, (int)(radius * waveDensity * 2));

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

            float wavePosition = (float)i / segments;
            float waveOffset = (animationPhase * waveSpeed + wavePosition) % 1f;

            Color segmentColor = getMultiGradientColor(gradientColors, waveOffset, alpha);

            drawList.addTriangleFilled(
                    centerX, centerY,
                    centerX + (float)Math.cos(angle1) * radius, centerY + (float)Math.sin(angle1) * radius,
                    centerX + (float)Math.cos(angle2) * radius, centerY + (float)Math.sin(angle2) * radius,
                    new ImColorW(segmentColor).packed()
            );
        }
    }

    public static Color getColorFromSetting(ColorSetting setting) {
        float[] colorValues = setting.get();
        return new Color(
                colorValues[0],
                colorValues[1],
                colorValues[2],
                colorValues[3]
        );
    }

    public static Color getMultiGradientColor(Color[] colors, float factor, int alpha) {
        if (colors.length == 1) return new Color(colors[0].getRed(), colors[0].getGreen(), colors[0].getBlue(), alpha);

        float segmentSize = 1f / (colors.length - 1);
        int segment = (int) (factor / segmentSize);
        segment = Math.min(segment, colors.length - 2);

        float localFactor = (factor - segment * segmentSize) / segmentSize;

        return interpolateColor(colors[segment], colors[segment + 1], localFactor, alpha);
    }

    private static Color interpolateColor(Color color1, Color color2, float factor, int alpha) {
        factor = Math.max(0, Math.min(1, factor));

        int red = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * factor);
        int green = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * factor);
        int blue = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * factor);

        return new Color(red, green, blue, alpha);
    }

    public float getAnimationPhase() {
        return animationPhase;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public void resetAnimation() {
        this.startTime = System.currentTimeMillis();
        this.animationPhase = 0f;
    }
}