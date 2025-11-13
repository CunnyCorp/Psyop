package monster.psyop.client.framework.gui.themes;

import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import lombok.Getter;

@Getter
public class OfficialTheme extends DefaultV2Theme {
    private final float[] softColor = new float[]{0.00f, 0.75f, 0.75f, 1.00f}; // Light cyan
    private final float[] harshColor = new float[]{0.85f, 0.35f, 0.75f, 1.00f}; // Light magenta

    @Override
    public void load(ImGuiStyle style) {
        super.load(style);
        style.setColor(ImGuiCol.WindowBg, 0.09f, 0.09f, 0.11f, 1.00f);
        style.setColor(ImGuiCol.ChildBg, 0.10f, 0.10f, 0.12f, 0.50f);
        style.setColor(ImGuiCol.PopupBg, 0.12f, 0.12f, 0.14f, 0.94f);
        style.setColor(ImGuiCol.MenuBarBg, 0.11f, 0.11f, 0.13f, 1.00f);
        style.setColor(ImGuiCol.Text, 0.90f, 0.90f, 0.95f, 1.00f);
        style.setColor(ImGuiCol.TextDisabled, 0.50f, 0.50f, 0.55f, 1.00f);
        style.setColor(ImGuiCol.Border, 0.25f, 0.25f, 0.28f, 0.50f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg, 0.16f, 0.16f, 0.19f, 1.00f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.20f, 0.20f, 0.23f, 1.00f);
        style.setColor(ImGuiCol.FrameBgActive, 0.22f, 0.22f, 0.25f, 1.00f);
        style.setColor(ImGuiCol.TitleBg, 0.11f, 0.11f, 0.13f, 1.00f);
        style.setColor(ImGuiCol.TitleBgActive, 0.13f, 0.13f, 0.15f, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.11f, 0.11f, 0.13f, 0.75f);
        style.setColor(ImGuiCol.CheckMark, 0.00f, 0.75f, 0.75f, 1.00f); // Cyan
        style.setColor(ImGuiCol.SliderGrab, 0.85f, 0.35f, 0.75f, 0.60f); // Magenta
        style.setColor(ImGuiCol.SliderGrabActive, 0.85f, 0.35f, 0.75f, 0.80f); // Magenta
        style.setColor(ImGuiCol.Button, 0.18f, 0.18f, 0.21f, 1.00f);
        style.setColor(ImGuiCol.ButtonHovered, 0.85f, 0.35f, 0.75f, 0.30f); // Magenta tint
        style.setColor(ImGuiCol.ButtonActive, 0.00f, 0.75f, 0.75f, 0.30f); // Cyan tint
        style.setColor(ImGuiCol.Header, 0.85f, 0.35f, 0.75f, 0.31f); // Magenta
        style.setColor(ImGuiCol.HeaderHovered, 0.85f, 0.35f, 0.75f, 0.51f); // Magenta
        style.setColor(ImGuiCol.HeaderActive, 0.00f, 0.75f, 0.75f, 0.51f); // Cyan
        style.setColor(ImGuiCol.Separator, 0.25f, 0.25f, 0.28f, 0.50f);
        style.setColor(ImGuiCol.SeparatorHovered, 0.85f, 0.35f, 0.75f, 0.78f); // Magenta
        style.setColor(ImGuiCol.SeparatorActive, 0.00f, 0.75f, 0.75f, 1.00f); // Cyan
        style.setColor(ImGuiCol.ResizeGrip, 0.85f, 0.35f, 0.75f, 0.20f); // Magenta
        style.setColor(ImGuiCol.ResizeGripHovered, 0.85f, 0.35f, 0.75f, 0.67f); // Magenta
        style.setColor(ImGuiCol.ResizeGripActive, 0.00f, 0.75f, 0.75f, 0.95f); // Cyan
        style.setColor(ImGuiCol.Tab, 0.13f, 0.13f, 0.15f, 0.86f);
        style.setColor(ImGuiCol.TabHovered, 0.85f, 0.35f, 0.75f, 0.80f); // Magenta
        style.setColor(ImGuiCol.TabActive, 0.00f, 0.75f, 0.75f, 0.80f); // Cyan
        style.setColor(ImGuiCol.TabUnfocused, 0.11f, 0.11f, 0.13f, 0.97f);
        style.setColor(ImGuiCol.TabUnfocusedActive, 0.13f, 0.13f, 0.15f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.10f, 0.10f, 0.12f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.85f, 0.35f, 0.75f, 0.31f); // Magenta
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.85f, 0.35f, 0.75f, 0.67f); // Magenta
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.00f, 0.75f, 0.75f, 0.95f); // Cyan
        style.setColor(ImGuiCol.DockingPreview, 0.00f, 0.75f, 0.75f, 0.70f); // Cyan
        style.setColor(ImGuiCol.DockingEmptyBg, 0.20f, 0.20f, 0.20f, 1.00f);
        style.setColor(ImGuiCol.TextSelectedBg, 0.85f, 0.35f, 0.75f, 0.35f); // Magenta
        style.setColor(ImGuiCol.DragDropTarget, 0.00f, 0.75f, 0.75f, 0.95f); // Cyan
        style.setColor(ImGuiCol.NavHighlight, 0.00f, 0.75f, 0.75f, 1.00f); // Cyan
        style.setColor(ImGuiCol.NavWindowingHighlight, 0.85f, 0.35f, 0.75f, 0.70f); // Magenta
        style.setColor(ImGuiCol.NavWindowingDimBg, 0.80f, 0.80f, 0.80f, 0.20f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.04f, 0.10f, 0.09f, 0.51f);
        style.setColor(ImGuiCol.TableHeaderBg, 0.13f, 0.13f, 0.15f, 1.00f);
        style.setColor(ImGuiCol.TableBorderStrong, 0.25f, 0.25f, 0.28f, 1.00f);
        style.setColor(ImGuiCol.TableBorderLight, 0.20f, 0.20f, 0.23f, 1.00f);
        style.setColor(ImGuiCol.TableRowBg, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.TableRowBgAlt, 1.00f, 1.00f, 1.00f, 0.06f);
        style.setColor(ImGuiCol.PlotLines, 0.00f, 0.75f, 0.75f, 1.00f); // Cyan
        style.setColor(ImGuiCol.PlotLinesHovered, 0.85f, 0.35f, 0.75f, 1.00f); // Magenta
        style.setColor(ImGuiCol.PlotHistogram, 0.00f, 0.75f, 0.75f, 1.00f); // Cyan
        style.setColor(ImGuiCol.PlotHistogramHovered, 0.85f, 0.35f, 0.75f, 1.00f); // Magenta
    }
}