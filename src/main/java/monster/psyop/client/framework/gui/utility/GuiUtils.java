package monster.psyop.client.framework.gui.utility;

import imgui.ImGui;
import imgui.ImVec2;

public class GuiUtils {
    public static void text(ColoredText coloredText) {
        ImGui.textColored(
                coloredText.color().getRGB(),
                coloredText.text());
    }

    public static void text(ColoredText... coloredTexts) {
        if (coloredTexts == null) return;

        boolean sameLine = false;

        for (ColoredText coloredText : coloredTexts) {
            if (sameLine) ImGui.sameLine();
            ImGui.textColored(coloredText.color().getRGB(), coloredText.text());
            sameLine = true;
        }
    }

    public static void drawRectFilled(ImVec2 cornerMin, ImVec2 cornerMax, int color, float rounding) {
        ImGui.getWindowDrawList().addRectFilled(
                cornerMin.x, cornerMin.y,
                cornerMax.x, cornerMax.y,
                color,
                rounding
        );
    }

    public static boolean isHovering(ImVec2 cornerMin, ImVec2 cornerMax) {
        return ImGui.isMouseHoveringRect(cornerMin.x, cornerMin.y, cornerMax.x, cornerMax.y);
    }
}
