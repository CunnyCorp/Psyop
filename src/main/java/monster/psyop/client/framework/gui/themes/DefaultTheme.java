package monster.psyop.client.framework.gui.themes;

import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;

import java.awt.*;

public class DefaultTheme {
    public void load(ImGuiStyle style) {
        style.setAlpha(0.78f);
        style.setChildRounding(6);
        style.setTabRounding(3);
        style.setGrabRounding(7);
        style.setFrameRounding(7);
        style.setScrollbarRounding(8);
        style.setPopupRounding(5);
        style.setGrabMinSize(20f);
        style.setFrameBorderSize(0.3f);
        style.setTabBorderSize(0.3f);
        style.setChildBorderSize(0.3f);
        style.setWindowRounding(0.0f);
        style.setButtonTextAlign(0.5f, 0.5f);
        style.setWindowTitleAlign(0.5f, 0.5f);
        style.setWindowMenuButtonPosition(ImGuiDir.Right);
        style.setColorButtonPosition(ImGuiDir.Left);
        style.setItemSpacing(4, 1);
        style.setFramePadding(4, 3);
        style.setWindowPadding(6, 5);

        style.setColor(ImGuiCol.Text, new Color(218, 68, 223, 255).getRGB());
        style.setColor(ImGuiCol.TextDisabled, new Color(101, 218, 174, 255).getRGB());
        style.setColor(ImGuiCol.WindowBg, new Color(14, 14, 14, 255).getRGB());
        style.setColor(ImGuiCol.ChildBg, 0.0f, 0.0f, 0.0f, 0.0f);
        style.setColor(ImGuiCol.Border, 0.0f, 1.0f, 1.0f, 0.65f);
        style.setColor(ImGuiCol.BorderShadow, 0.0f, 0.0f, 0.0f, 0.0f);
        style.setColor(ImGuiCol.FrameBg, 0.44f, 0.80f, 0.80f, 0.18f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.44f, 0.80f, 0.80f, 0.27f);
        style.setColor(ImGuiCol.FrameBgActive, 0.44f, 0.81f, 0.86f, 0.66f);
        style.setColor(ImGuiCol.TitleBg, new Color(0, 0, 0, 192).getRGB());
        style.setColor(ImGuiCol.TitleBgCollapsed, new Color(0, 0, 0, 163).getRGB());
        style.setColor(ImGuiCol.TitleBgActive, new Color(12, 15, 68, 175).getRGB());
        style.setColor(ImGuiCol.MenuBarBg, 0.0f, 0.0f, 0.0f, 0.20f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.22f, 0.29f, 0.30f, 0.71f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.0f, 1.0f, 1.0f, 0.44f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.0f, 1.0f, 1.0f, 0.74f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.CheckMark, 0.0f, 1.0f, 1.0f, 0.68f);
        style.setColor(ImGuiCol.SliderGrab, 0.0f, 1.0f, 1.0f, 0.36f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.0f, 1.0f, 1.0f, 0.76f);
        style.setColor(ImGuiCol.Tab, new Color(105, 92, 110, 117).getRGB());
        style.setColor(ImGuiCol.TabUnfocused, new Color(41, 9, 42, 117).getRGB());
        style.setColor(ImGuiCol.TabHovered, new Color(143, 53, 161, 158).getRGB());
        style.setColor(ImGuiCol.TabActive, new Color(42, 157, 201, 113).getRGB());
        style.setColor(ImGuiCol.TabUnfocusedActive, new Color(42, 157, 201, 113).getRGB());
        style.setColor(ImGuiCol.Button, new Color(105, 92, 110, 117).getRGB());
        style.setColor(ImGuiCol.ButtonHovered, new Color(143, 53, 161, 158).getRGB());
        style.setColor(ImGuiCol.ButtonActive, new Color(42, 157, 201, 113).getRGB());
        style.setColor(ImGuiCol.Header, 0.0f, 1.0f, 1.0f, 0.33f);
        style.setColor(ImGuiCol.HeaderHovered, new Color(143, 53, 161, 158).getRGB());
        style.setColor(ImGuiCol.HeaderActive, new Color(42, 157, 201, 113).getRGB());
        style.setColor(ImGuiCol.ResizeGrip, 0.0f, 1.0f, 1.0f, 0.54f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.0f, 1.0f, 1.0f, 0.74f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.PlotLines, 0.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.PlotLinesHovered, 0.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.PlotHistogram, 0.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.PlotHistogramHovered, 0.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.TextSelectedBg, new Color(227, 155, 226, 128).getRGB());
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.04f, 0.10f, 0.09f, 0.51f);
    }
}
