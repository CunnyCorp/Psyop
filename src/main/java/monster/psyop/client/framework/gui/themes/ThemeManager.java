package monster.psyop.client.framework.gui.themes;

import imgui.ImGui;
import monster.psyop.client.Liberty;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeManager {
    public static final Map<String, Integer> STRING_TO_COL = new HashMap<>();
    private static final String[] COL_NAMES = new String[]{"Text", "TextDisabled", "WindowBg", "ChildBg", "PopupBg", "Border", "BorderShadow", "FrameBg", "FrameBgHovered", "FrameBgActive", "TitleBg", "TitleBgActive", "TitleBgCollapsed", "MenuBarBg", "ScrollbarBg", "ScrollbarGrab", "ScrollbarGrabHovered", "ScrollbarGrabActive", "CheckMark", "SliderGrab", "SliderGrabActive", "Button", "ButtonHovered", "ButtonActive", "Header", "HeaderHovered", "HeaderActive", "Separator", "SeparatorHovered", "SeparatorActive", "ResizeGrip", "ResizeGripHovered", "ResizeGripActive", "Tab", "TabHovered", "TabActive", "TabUnfocused", "TabUnfocusedActive", "DockingPreview", "DockingEmptyBg", "PlotLines", "PlotLinesHovered", "PlotHistogram", "PlotHistogramHovered", "TableHeaderBg", "TableBorderStrong", "TableBorderLight", "TableRowBg", "TableRowBgAlt", "TextSelectedBg", "DragDropTarget", "NavHighlight", "NavWindowingHighlight", "NavWindowingDimBg", "ModalWindowDimBg"};

    public static void init() {
        int pos = 0;
        for (String col : COL_NAMES) {
            STRING_TO_COL.put(col, pos++);
        }
    }

    public static void loadTheme(DefaultTheme theme) {
        theme.load(ImGui.getStyle());
    }

    public static void loadTheme(Path file) {
        if (Files.exists(file) && Files.isRegularFile(file)) {
            try {
                List<String> lines = Files.readAllLines(file);

                for (String line : lines) {
                    line = line.trim();

                    String[] parts = line.split("=");

                    if (parts.length == 2) {
                        if (parts[0].startsWith("colors[")) {
                            String assignmentPart = parts[0].split("ImGuiCol_")[1].replace("]", "").trim();

                            if (STRING_TO_COL.containsKey(assignmentPart)) {
                                String[] colParts = parts[1].replace("ImVec4(", "").replace(");", "").replaceAll("f", "").trim().split(",");

                                float[] color = new float[4];

                                int colPos = 0;
                                for (String col : colParts) {
                                    color[colPos++] = Float.parseFloat(col.trim());
                                }

                                ImGui.getStyle().setColor(STRING_TO_COL.get(assignmentPart), new ImColorW(color).packed());
                            } else {
                                Liberty.warn("The color {} was not defined.", assignmentPart);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Liberty.LOG.error("An exception occurred loading a theme.", e);
            }
        }
    }
}
