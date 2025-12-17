package monster.psyop.client.impl.modules.client;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;

public class GUIModule extends Module {
    public static GUIModule INSTANCE;
    public ColorSetting hudBg = new ColorSetting.Builder()
            .name("hud-bg")
            .defaultTo(new float[]{0.09f, 0.09f, 0.11f, 0.80f})
            .addTo(coreGroup);
    public ColorSetting hudBorder = new ColorSetting.Builder()
            .name("hud-border")
            .defaultTo(new float[]{0.0f, 0.0f, 0.0f, 1.0f})
            .addTo(coreGroup);
    public ColorSetting hudText = new ColorSetting.Builder()
            .name("hud-text")
            .defaultTo(new float[]{0.90f, 0.90f, 0.95f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting tooltipColor = new ColorSetting.Builder()
            .name("tooltip-color")
            .defaultTo(new float[]{1.0f, 1.0f, 0.86f, 1.0f})
            .addTo(coreGroup);
    public ColorSetting windowBg = new ColorSetting.Builder()
            .name("window-bg")
            .defaultTo(new float[]{0.09f, 0.09f, 0.11f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting childBg = new ColorSetting.Builder()
            .name("child-bg")
            .defaultTo(new float[]{0.10f, 0.10f, 0.12f, 0.50f})
            .addTo(coreGroup);
    public ColorSetting popupBg = new ColorSetting.Builder()
            .name("popup-bg")
            .defaultTo(new float[]{0.12f, 0.12f, 0.14f, 0.94f})
            .addTo(coreGroup);
    public ColorSetting menuBarBg = new ColorSetting.Builder()
            .name("menu-bar-bg")
            .defaultTo(new float[]{0.11f, 0.11f, 0.13f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting text = new ColorSetting.Builder()
            .name("text")
            .defaultTo(new float[]{0.90f, 0.90f, 0.95f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting textDisabled = new ColorSetting.Builder()
            .name("text-disabled")
            .defaultTo(new float[]{0.50f, 0.50f, 0.55f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting border = new ColorSetting.Builder()
            .name("border")
            .defaultTo(new float[]{0.25f, 0.25f, 0.28f, 0.50f})
            .addTo(coreGroup);
    public ColorSetting borderShadow = new ColorSetting.Builder()
            .name("border-shadow")
            .defaultTo(new float[]{0.00f, 0.00f, 0.00f, 0.00f})
            .addTo(coreGroup);
    public ColorSetting frameBg = new ColorSetting.Builder()
            .name("frame-bg")
            .defaultTo(new float[]{0.16f, 0.16f, 0.19f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting frameBgHovered = new ColorSetting.Builder()
            .name("frame-bg-hovered")
            .defaultTo(new float[]{0.20f, 0.20f, 0.23f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting frameBgActive = new ColorSetting.Builder()
            .name("frame-bg-active")
            .defaultTo(new float[]{0.22f, 0.22f, 0.25f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting titleBg = new ColorSetting.Builder()
            .name("title-bg")
            .defaultTo(new float[]{0.11f, 0.11f, 0.13f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting titleBgActive = new ColorSetting.Builder()
            .name("title-bg-active")
            .defaultTo(new float[]{0.13f, 0.13f, 0.15f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting titleBgCollapsed = new ColorSetting.Builder()
            .name("title-bg-collapsed")
            .defaultTo(new float[]{0.11f, 0.11f, 0.13f, 0.75f})
            .addTo(coreGroup);
    public ColorSetting checkMark = new ColorSetting.Builder()
            .name("check-mark")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting sliderGrab = new ColorSetting.Builder()
            .name("slider-grab")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.60f})
            .addTo(coreGroup);
    public ColorSetting sliderGrabActive = new ColorSetting.Builder()
            .name("slider-grab-active")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.80f})
            .addTo(coreGroup);
    public ColorSetting button = new ColorSetting.Builder()
            .name("button")
            .defaultTo(new float[]{0.18f, 0.18f, 0.21f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting buttonHovered = new ColorSetting.Builder()
            .name("button-hovered")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.30f})
            .addTo(coreGroup);
    public ColorSetting buttonActive = new ColorSetting.Builder()
            .name("button-active")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 0.30f})
            .addTo(coreGroup);
    public ColorSetting header = new ColorSetting.Builder()
            .name("header")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.31f})
            .addTo(coreGroup);
    public ColorSetting headerHovered = new ColorSetting.Builder()
            .name("header-hovered")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.51f})
            .addTo(coreGroup);
    public ColorSetting headerActive = new ColorSetting.Builder()
            .name("header-active")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 0.51f})
            .addTo(coreGroup);
    public ColorSetting separator = new ColorSetting.Builder()
            .name("separator")
            .defaultTo(new float[]{0.25f, 0.25f, 0.28f, 0.50f})
            .addTo(coreGroup);
    public ColorSetting separatorHovered = new ColorSetting.Builder()
            .name("separator-hovered")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.78f})
            .addTo(coreGroup);
    public ColorSetting separatorActive = new ColorSetting.Builder()
            .name("separator-active")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting resizeGrip = new ColorSetting.Builder()
            .name("resize-grip")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.20f})
            .addTo(coreGroup);
    public ColorSetting resizeGripHovered = new ColorSetting.Builder()
            .name("resize-grip-hovered")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.67f})
            .addTo(coreGroup);
    public ColorSetting resizeGripActive = new ColorSetting.Builder()
            .name("resize-grip-active")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 0.95f})
            .addTo(coreGroup);
    public ColorSetting tab = new ColorSetting.Builder()
            .name("tab")
            .defaultTo(new float[]{0.13f, 0.13f, 0.15f, 0.86f})
            .addTo(coreGroup);
    public ColorSetting tabHovered = new ColorSetting.Builder()
            .name("tab-hovered")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.80f})
            .addTo(coreGroup);
    public ColorSetting tabActive = new ColorSetting.Builder()
            .name("tab-active")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 0.80f})
            .addTo(coreGroup);
    public ColorSetting tabUnfocused = new ColorSetting.Builder()
            .name("tab-unfocused")
            .defaultTo(new float[]{0.11f, 0.11f, 0.13f, 0.97f})
            .addTo(coreGroup);
    public ColorSetting tabUnfocusedActive = new ColorSetting.Builder()
            .name("tab-unfocused-active")
            .defaultTo(new float[]{0.13f, 0.13f, 0.15f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting scrollbarBg = new ColorSetting.Builder()
            .name("scrollbar-bg")
            .defaultTo(new float[]{0.10f, 0.10f, 0.12f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting scrollbarGrab = new ColorSetting.Builder()
            .name("scrollbar-grab")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.31f})
            .addTo(coreGroup);
    public ColorSetting scrollbarGrabHovered = new ColorSetting.Builder()
            .name("scrollbar-grab-hovered")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.67f})
            .addTo(coreGroup);
    public ColorSetting scrollbarGrabActive = new ColorSetting.Builder()
            .name("scrollbar-grab-active")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 0.95f})
            .addTo(coreGroup);
    public ColorSetting dockingPreview = new ColorSetting.Builder()
            .name("docking-preview")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 0.70f})
            .addTo(coreGroup);
    public ColorSetting dockingEmptyBg = new ColorSetting.Builder()
            .name("docking-empty-bg")
            .defaultTo(new float[]{0.20f, 0.20f, 0.20f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting textSelectedBg = new ColorSetting.Builder()
            .name("text-selected-bg")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.35f})
            .addTo(coreGroup);
    public ColorSetting dragDropTarget = new ColorSetting.Builder()
            .name("drag-drop-target")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 0.95f})
            .addTo(coreGroup);
    public ColorSetting navHighlight = new ColorSetting.Builder()
            .name("nav-highlight")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting navWindowingHighlight = new ColorSetting.Builder()
            .name("nav-windowing-highlight")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 0.70f})
            .addTo(coreGroup);
    public ColorSetting navWindowingDimBg = new ColorSetting.Builder()
            .name("nav-windowing-dim-bg")
            .defaultTo(new float[]{0.80f, 0.80f, 0.80f, 0.20f})
            .addTo(coreGroup);
    public ColorSetting modalWindowDimBg = new ColorSetting.Builder()
            .name("modal-window-dim-bg")
            .defaultTo(new float[]{0.04f, 0.10f, 0.09f, 0.51f})
            .addTo(coreGroup);
    public ColorSetting tableHeaderBg = new ColorSetting.Builder()
            .name("table-header-bg")
            .defaultTo(new float[]{0.13f, 0.13f, 0.15f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting tableBorderStrong = new ColorSetting.Builder()
            .name("table-border-strong")
            .defaultTo(new float[]{0.25f, 0.25f, 0.28f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting tableBorderLight = new ColorSetting.Builder()
            .name("table-border-light")
            .defaultTo(new float[]{0.20f, 0.20f, 0.23f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting tableRowBg = new ColorSetting.Builder()
            .name("table-row-bg")
            .defaultTo(new float[]{0.00f, 0.00f, 0.00f, 0.00f})
            .addTo(coreGroup);
    public ColorSetting tableRowBgAlt = new ColorSetting.Builder()
            .name("table-row-bg-alt")
            .defaultTo(new float[]{1.00f, 1.00f, 1.00f, 0.06f})
            .addTo(coreGroup);
    public ColorSetting plotLines = new ColorSetting.Builder()
            .name("plot-lines")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting plotLinesHovered = new ColorSetting.Builder()
            .name("plot-lines-hovered")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting plotHistogram = new ColorSetting.Builder()
            .name("plot-histogram")
            .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.00f})
            .addTo(coreGroup);
    public ColorSetting plotHistogramHovered = new ColorSetting.Builder()
            .name("plot-histogram-hovered")
            .defaultTo(new float[]{0.85f, 0.35f, 0.75f, 1.00f})
            .addTo(coreGroup);

    public GUIModule() {
        super(Categories.CLIENT, "GUI", "Manage GUI options!");
        INSTANCE = this;
    }

    public void applyStyleColors() {
        ImGuiStyle style = ImGui.getStyle();

        style.setColor(ImGuiCol.WindowBg, windowBg.getColor());
        style.setColor(ImGuiCol.ChildBg, childBg.getColor());
        style.setColor(ImGuiCol.PopupBg, popupBg.getColor());
        style.setColor(ImGuiCol.MenuBarBg, menuBarBg.getColor());
        style.setColor(ImGuiCol.Text, text.getColor());
        style.setColor(ImGuiCol.TextDisabled, textDisabled.getColor());
        style.setColor(ImGuiCol.Border, border.getColor());
        style.setColor(ImGuiCol.BorderShadow, borderShadow.getColor());
        style.setColor(ImGuiCol.FrameBg, frameBg.getColor());
        style.setColor(ImGuiCol.FrameBgHovered, frameBgHovered.getColor());
        style.setColor(ImGuiCol.FrameBgActive, frameBgActive.getColor());
        style.setColor(ImGuiCol.TitleBg, titleBg.getColor());
        style.setColor(ImGuiCol.TitleBgActive, titleBgActive.getColor());
        style.setColor(ImGuiCol.TitleBgCollapsed, titleBgCollapsed.getColor());
        style.setColor(ImGuiCol.CheckMark, checkMark.getColor());
        style.setColor(ImGuiCol.SliderGrab, sliderGrab.getColor());
        style.setColor(ImGuiCol.SliderGrabActive, sliderGrabActive.getColor());
        style.setColor(ImGuiCol.Button, button.getColor());
        style.setColor(ImGuiCol.ButtonHovered, buttonHovered.getColor());
        style.setColor(ImGuiCol.ButtonActive, buttonActive.getColor());
        style.setColor(ImGuiCol.Header, header.getColor());
        style.setColor(ImGuiCol.HeaderHovered, headerHovered.getColor());
        style.setColor(ImGuiCol.HeaderActive, headerActive.getColor());
        style.setColor(ImGuiCol.Separator, separator.getColor());
        style.setColor(ImGuiCol.SeparatorHovered, separatorHovered.getColor());
        style.setColor(ImGuiCol.SeparatorActive, separatorActive.getColor());
        style.setColor(ImGuiCol.ResizeGrip, resizeGrip.getColor());
        style.setColor(ImGuiCol.ResizeGripHovered, resizeGripHovered.getColor());
        style.setColor(ImGuiCol.ResizeGripActive, resizeGripActive.getColor());
        style.setColor(ImGuiCol.Tab, tab.getColor());
        style.setColor(ImGuiCol.TabHovered, tabHovered.getColor());
        style.setColor(ImGuiCol.TabActive, tabActive.getColor());
        style.setColor(ImGuiCol.TabUnfocused, tabUnfocused.getColor());
        style.setColor(ImGuiCol.TabUnfocusedActive, tabUnfocusedActive.getColor());
        style.setColor(ImGuiCol.ScrollbarBg, scrollbarBg.getColor());
        style.setColor(ImGuiCol.ScrollbarGrab, scrollbarGrab.getColor());
        style.setColor(ImGuiCol.ScrollbarGrabHovered, scrollbarGrabHovered.getColor());
        style.setColor(ImGuiCol.ScrollbarGrabActive, scrollbarGrabActive.getColor());
        style.setColor(ImGuiCol.DockingPreview, dockingPreview.getColor());
        style.setColor(ImGuiCol.DockingEmptyBg, dockingEmptyBg.getColor());
        style.setColor(ImGuiCol.TextSelectedBg, textSelectedBg.getColor());
        style.setColor(ImGuiCol.DragDropTarget, dragDropTarget.getColor());
        style.setColor(ImGuiCol.NavHighlight, navHighlight.getColor());
        style.setColor(ImGuiCol.NavWindowingHighlight, navWindowingHighlight.getColor());
        style.setColor(ImGuiCol.NavWindowingDimBg, navWindowingDimBg.getColor());
        style.setColor(ImGuiCol.ModalWindowDimBg, modalWindowDimBg.getColor());
        style.setColor(ImGuiCol.TableHeaderBg, tableHeaderBg.getColor());
        style.setColor(ImGuiCol.TableBorderStrong, tableBorderStrong.getColor());
        style.setColor(ImGuiCol.TableBorderLight, tableBorderLight.getColor());
        style.setColor(ImGuiCol.TableRowBg, tableRowBg.getColor());
        style.setColor(ImGuiCol.TableRowBgAlt, tableRowBgAlt.getColor());
        style.setColor(ImGuiCol.PlotLines, plotLines.getColor());
        style.setColor(ImGuiCol.PlotLinesHovered, plotLinesHovered.getColor());
        style.setColor(ImGuiCol.PlotHistogram, plotHistogram.getColor());
        style.setColor(ImGuiCol.PlotHistogramHovered, plotHistogramHovered.getColor());
    }
}
