package monster.psyop.client.utility;

import imgui.ImVec4;

/**
 * Utility class for animation functions and easing curves
 */
public class AnimationUtils {

    // Linear interpolation between two values
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.min(1, Math.max(0, t));
    }

    // Linear interpolation between two ImVec4 colors
    public static ImVec4 lerp(ImVec4 a, ImVec4 b, float t) {
        t = Math.min(1, Math.max(0, t));
        return new ImVec4(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.z + (b.z - a.z) * t,
                a.w + (b.w - a.w) * t
        );
    }

    // Linear interpolation between two colors (packed as 0xAARRGGBB integers)
    public static int lerpColor(int color1, int color2, float t) {
        t = Math.min(1, Math.max(0, t));

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // Easing functions (all take t between 0-1 and return eased value)

    // Quadratic ease-in
    public static float easeInQuad(float t) {
        return t * t;
    }

    // Quadratic ease-out
    public static float easeOutQuad(float t) {
        return t * (2 - t);
    }

    // Quadratic ease-in-out
    public static float easeInOutQuad(float t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    // Cubic ease-in
    public static float easeInCubic(float t) {
        return t * t * t;
    }

    // Cubic ease-out
    public static float easeOutCubic(float t) {
        return (--t) * t * t + 1;
    }

    // Cubic ease-in-out
    public static float easeInOutCubic(float t) {
        return t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }

    // Elastic ease-out (bouncy effect)
    public static float easeOutElastic(float t) {
        float c4 = (2 * (float) Math.PI) / 3;
        return t == 0 ? 0 : t == 1 ? 1 : (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75) * c4) + 1;
    }

    // Bounce ease-out
    public static float easeOutBounce(float t) {
        if (t < (1 / 2.75)) {
            return 7.5625f * t * t;
        } else if (t < (2 / 2.75)) {
            return 7.5625f * (t -= (1.5f / 2.75f)) * t + 0.75f;
        } else if (t < (2.5 / 2.75)) {
            return 7.5625f * (t -= (2.25f / 2.75f)) * t + 0.9375f;
        } else {
            return 7.5625f * (t -= (2.625f / 2.75f)) * t + 0.984375f;
        }
    }

    // Spring easing (with customizable tension and friction)
    public static float spring(float t, float tension, float friction) {
        return (float) (1 - Math.exp(-tension * t) * Math.cos(friction * t));
    }

    // Pulse animation (oscillates between 0 and 1)
    public static float pulse(float time, float speed) {
        return (float) ((Math.sin(time * speed * Math.PI * 2) + 1) / 2);
    }

    // Ping-pong animation (goes back and forth between 0 and 1)
    public static float pingPong(float time, float speed) {
        return Math.abs((time * speed) % 2 - 1);
    }

    // Smooth step function
    public static float smoothStep(float edge0, float edge1, float x) {
        x = Math.min(1, Math.max(0, (x - edge0) / (edge1 - edge0)));
        return x * x * (3 - 2 * x);
    }

    // Smoother step function
    public static float smootherStep(float edge0, float edge1, float x) {
        x = Math.min(1, Math.max(0, (x - edge0) / (edge1 - edge0)));
        return x * x * x * (x * (x * 6 - 15) + 10);
    }

    // Remap a value from one range to another
    public static float remap(float value, float fromMin, float fromMax, float toMin, float toMax) {
        return toMin + (value - fromMin) * (toMax - toMin) / (fromMax - fromMin);
    }

    // Clamp a value between min and max
    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));
    }

    // Normalize a value from 0 to 1 based on min and max
    public static float normalize(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    // Calculate animation progress with easing
    public static float animate(float currentTime, float startTime, float duration, EasingFunction easing) {
        float progress = (currentTime - startTime) / duration;
        progress = clamp(progress, 0, 1);
        return easing != null ? easing.ease(progress) : progress;
    }

    // Create a color with alpha
    public static int withAlpha(int color, float alpha) {
        int a = (int) (alpha * 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    // Darken a color by percentage
    public static int darken(int color, float percent) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        r = (int) (r * (1 - percent));
        g = (int) (g * (1 - percent));
        b = (int) (b * (1 - percent));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // Lighten a color by percentage
    public static int lighten(int color, float percent) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        r = (int) (r + (255 - r) * percent);
        g = (int) (g + (255 - g) * percent);
        b = (int) (b + (255 - b) * percent);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // Convert HSV to RGB color (h: 0-360, s: 0-1, v: 0-1)
    public static int hsvToRgb(float h, float s, float v) {
        h = h % 360;
        if (h < 0) h += 360;

        int hi = (int) (h / 60) % 6;
        float f = h / 60 - hi;

        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        float r, g, b;
        switch (hi) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            default:
                r = v;
                g = p;
                b = q;
                break;
        }

        return 0xFF000000 |
                ((int) (r * 255) << 16) |
                ((int) (g * 255) << 8) |
                (int) (b * 255);
    }

    // Functional interface for custom easing
    @FunctionalInterface
    public interface EasingFunction {
        float ease(float t);
    }

    // Pre-defined easing functions
    public static final EasingFunction LINEAR = t -> t;
    public static final EasingFunction QUAD_IN = AnimationUtils::easeInQuad;
    public static final EasingFunction QUAD_OUT = AnimationUtils::easeOutQuad;
    public static final EasingFunction QUAD_IN_OUT = AnimationUtils::easeInOutQuad;
    public static final EasingFunction CUBIC_IN = AnimationUtils::easeInCubic;
    public static final EasingFunction CUBIC_OUT = AnimationUtils::easeOutCubic;
    public static final EasingFunction CUBIC_IN_OUT = AnimationUtils::easeInOutCubic;
    public static final EasingFunction ELASTIC_OUT = AnimationUtils::easeOutElastic;
    public static final EasingFunction BOUNCE_OUT = AnimationUtils::easeOutBounce;
}