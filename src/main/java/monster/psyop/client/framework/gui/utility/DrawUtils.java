package monster.psyop.client.framework.gui.utility;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;

public class DrawUtils {
    public static void drawLine(float x1, float y1, float x2, float y2, ImColorW color) {
        drawLine(x1, y1, x2, y2, color, 1.0f);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, ImColorW color, float thickness) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addLine(x1, y1, x2, y2, color.packed(), thickness);
    }

    public static void drawRect(float x, float y, float width, float height, ImColorW color) {
        drawRect(x, y, width, height, color, 0.0f, 1.0f);
    }

    public static void drawRect(float x, float y, float width, float height, ImColorW color, float rounding) {
        drawRect(x, y, width, height, color, rounding, 1.0f);
    }

    public static void drawRect(float x, float y, float width, float height, ImColorW color, float rounding, float thickness) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addRect(x, y, x + width, y + height, color.packed(), rounding, 0, thickness);
    }

    public static void drawRectFilled(float x, float y, float width, float height, ImColorW color) {
        drawRectFilled(x, y, width, height, color, 0.0f);
    }

    public static void drawRectFilled(float x, float y, float width, float height, ImColorW color, float rounding) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addRectFilled(x, y, x + width, y + height, color.packed(), rounding, 0);
    }

    public static void drawPolygon(int[] xPoints, int[] yPoints, int pointCount, ImColorW color) {
        if (xPoints.length != pointCount || yPoints.length != pointCount) {
            throw new IllegalArgumentException("Point arrays must match pointCount");
        }

        ImDrawList drawList = ImGui.getBackgroundDrawList();

        for (int i = 0; i < pointCount - 1; i++) {
            drawList.addLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], color.packed(), 1.0f);
        }
        if (pointCount > 1) {
            drawList.addLine(xPoints[pointCount - 1], yPoints[pointCount - 1], xPoints[0], yPoints[0], color.packed(), 1.0f);
        }
    }

    public static void drawCircle(float centerX, float centerY, float radius, ImColorW color) {
        drawCircle(centerX, centerY, radius, color, 12, 1.0f);
    }

    public static void drawCircle(float centerX, float centerY, float radius, ImColorW color, int segments, float thickness) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addCircle(centerX, centerY, radius, color.packed(), segments, thickness);
    }

    public static void drawCircleFilled(float centerX, float centerY, float radius, ImColorW color) {
        drawCircleFilled(centerX, centerY, radius, color, 12);
    }

    public static void drawCircleFilled(float centerX, float centerY, float radius, ImColorW color, int segments) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addCircleFilled(centerX, centerY, radius, color.packed(), segments);
    }

    public static void drawText(String text, float x, float y, ImColorW color) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addText(x, y, color.packed(), text);
    }

    public static void drawText(String text, float x, float y, ImColorW color, float scale) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addText(ImGui.getFont(), scale, x, y, color.packed(), text);
    }

    public static void drawTextWithBackground(String text, float x, float y, ImColorW textColor, ImColorW backgroundColor, float padding) {
        ImVec2 textSize = new ImVec2();
        ImGui.calcTextSize(textSize, text);

        drawRectFilled(x - padding, y - padding, textSize.x + (padding * 2), textSize.y + (padding * 2), backgroundColor);
        drawText(text, x, y, textColor);
    }
}