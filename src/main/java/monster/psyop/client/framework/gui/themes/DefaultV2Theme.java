package monster.psyop.client.framework.gui.themes;

import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import lombok.Getter;

@Getter
public class DefaultV2Theme extends DefaultTheme {
    private final float[] softColor = new float[]{0.61f, 0.93f, 0.90f, 1.00f};
    private final float[] harshColor = new float[]{0.71f, 0.73f, 0.82f, 1.00f};

    @Override
    public void load(ImGuiStyle style) {
        super.load(style);

        style.setColor(ImGuiCol.Text, 0.61f, 0.93f, 0.90f, 1.00f);
        style.setColor(ImGuiCol.TextDisabled, 0.71f, 0.73f, 0.82f, 1.00f);
        style.setColor(ImGuiCol.WindowBg, 0.01f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.ChildBg, 0.04f, 0.02f, 0.02f, 0.00f);
        style.setColor(ImGuiCol.PopupBg, 0.08f, 0.08f, 0.08f, 0.94f);
        style.setColor(ImGuiCol.Border, 0.97f, 0.08f, 0.08f, 0.65f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg, 0.27f, 0.24f, 0.43f, 0.18f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.29f, 0.33f, 0.75f, 0.27f);
        style.setColor(ImGuiCol.FrameBgActive, 0.63f, 0.54f, 0.80f, 0.66f);
        style.setColor(ImGuiCol.TitleBg, 0.00f, 0.00f, 0.00f, 0.75f);
        style.setColor(ImGuiCol.TitleBgActive, 0.27f, 0.06f, 0.05f, 0.69f);
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.00f, 0.00f, 0.00f, 0.64f);
        style.setColor(ImGuiCol.MenuBarBg, 0.00f, 0.00f, 0.00f, 0.20f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.22f, 0.29f, 0.30f, 0.71f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.88f, 0.05f, 0.05f, 0.44f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.86f, 0.11f, 0.11f, 0.74f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.86f, 0.26f, 0.26f, 1.00f);
        style.setColor(ImGuiCol.CheckMark, 0.29f, 0.82f, 0.92f, 0.68f);
        style.setColor(ImGuiCol.SliderGrab, 0.04f, 0.64f, 0.64f, 0.36f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.00f, 1.00f, 1.00f, 0.76f);
        style.setColor(ImGuiCol.Button, 0.43f, 0.36f, 0.41f, 0.46f);
        style.setColor(ImGuiCol.ButtonHovered, 0.63f, 0.21f, 0.56f, 0.62f);
        style.setColor(ImGuiCol.ButtonActive, 0.57f, 0.39f, 0.83f, 0.44f);
        style.setColor(ImGuiCol.Header, 0.00f, 1.00f, 1.00f, 0.33f);
        style.setColor(ImGuiCol.HeaderHovered, 0.63f, 0.21f, 0.56f, 0.62f);
        style.setColor(ImGuiCol.HeaderActive, 0.79f, 0.16f, 0.74f, 0.44f);
        style.setColor(ImGuiCol.Separator, 0.43f, 0.43f, 0.50f, 0.50f);
        style.setColor(ImGuiCol.SeparatorHovered, 0.10f, 0.40f, 0.75f, 0.78f);
        style.setColor(ImGuiCol.SeparatorActive, 0.10f, 0.40f, 0.75f, 1.00f);
        style.setColor(ImGuiCol.ResizeGrip, 0.92f, 0.27f, 0.22f, 0.54f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.82f, 0.26f, 0.41f, 0.74f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.61f, 0.92f, 0.88f, 1.00f);
        style.setColor(ImGuiCol.Tab, 0.20f, 0.05f, 0.08f, 0.15f);
        style.setColor(ImGuiCol.TabHovered, 0.86f, 0.36f, 0.37f, 0.62f);
        style.setColor(ImGuiCol.TabActive, 0.54f, 0.88f, 0.86f, 0.44f);
        style.setColor(ImGuiCol.TabUnfocused, 0.16f, 0.04f, 0.16f, 0.46f);
        style.setColor(ImGuiCol.TabUnfocusedActive, 0.79f, 0.62f, 0.16f, 0.44f);
        style.setColor(ImGuiCol.DockingPreview, 0.26f, 0.59f, 0.98f, 0.70f);
        style.setColor(ImGuiCol.DockingEmptyBg, 0.20f, 0.20f, 0.20f, 1.00f);
        style.setColor(ImGuiCol.PlotLines, 0.00f, 1.00f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.PlotLinesHovered, 0.00f, 1.00f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogram, 0.00f, 1.00f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogramHovered, 0.00f, 1.00f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.TableHeaderBg, 0.19f, 0.19f, 0.20f, 1.00f);
        style.setColor(ImGuiCol.TableBorderStrong, 0.31f, 0.31f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.TableBorderLight, 0.23f, 0.23f, 0.25f, 1.00f);
        style.setColor(ImGuiCol.TableRowBg, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.TableRowBgAlt, 1.00f, 1.00f, 1.00f, 0.06f);
        style.setColor(ImGuiCol.TextSelectedBg, 0.89f, 0.61f, 0.89f, 0.50f);
        style.setColor(ImGuiCol.DragDropTarget, 1.00f, 1.00f, 0.00f, 0.90f);
        style.setColor(ImGuiCol.NavHighlight, 0.26f, 0.59f, 0.98f, 1.00f);
        style.setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 1.00f, 1.00f, 0.70f);
        style.setColor(ImGuiCol.NavWindowingDimBg, 0.80f, 0.80f, 0.80f, 0.20f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.04f, 0.10f, 0.09f, 0.51f);
    }

}
