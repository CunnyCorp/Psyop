package monster.psyop.client.framework.modules.settings.wrappers;

import imgui.ImColor;

import java.awt.*;

public class ImColorW {
    private final transient float[] color = new float[]{0, 0, 0, 1};

    public ImColorW(Color color) {
        this.color[0] = color.getRed() / 255f;
        this.color[1] = color.getGreen() / 255f;
        this.color[2] = color.getBlue() / 255f;
        this.color[3] = color.getAlpha() / 255f;
    }

    public ImColorW(int r, int g, int b, int a) {
        this.color[0] = r / 255f;
        this.color[1] = g / 255f;
        this.color[2] = b / 255f;
        this.color[3] = a / 255f;
    }

    public ImColorW(float r, float g, float b, float a) {
        this.color[0] = r;
        this.color[1] = g;
        this.color[2] = b;
        this.color[3] = a;
    }

    public ImColorW(float[] color) {
        if (color.length < 3) throw new RuntimeException("Array had less than 3 values.");
        this.color[0] = color[0];
        this.color[1] = color[1];
        this.color[2] = color[2];
        if (color.length >= 4) this.color[3] = color[3];
    }

    public int packed() {
        return ImColor.rgba(color[0], color[1], color[2], color[3]);
    }
}
