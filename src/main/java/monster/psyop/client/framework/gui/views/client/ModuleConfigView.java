package monster.psyop.client.framework.gui.views.client;

import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImString;
import lombok.Getter;
import monster.psyop.client.Psyop;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.ModuleConfigSettings;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.framework.gui.utility.GuiUtils;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.utility.AnimationUtils;
import monster.psyop.client.utility.StringUtils;

import java.util.*;

public class ModuleConfigView extends View {
    public ModuleConfigSettings config;
    @Getter
    private Module module;
    private boolean awaitingWindowAdjustment;
    private GroupedSettings previewCategory;

    private float sidebarWidth = 140.0f;
    private boolean showCategoryHeaders = true;
    private boolean showSearchBar = true;
    private boolean showResetButtons = true;
    private boolean truncateLongCategoryNames = true;
    private int maxCategoryNameLength = 12;
    private float buttonHeight = 0.0f;
    private float itemSpacing = 6.0f;
    private float verticalSpacing = 4.0f;
    private float frameRounding = 4.0f;
    private float grabRounding = 3.0f;
    private float windowPaddingX = 8.0f;
    private float windowPaddingY = 8.0f;
    private float hoverBackgroundAlpha = 0.2f;
    private float hoverBackgroundRounding = 4.0f;
    private float animationSpeed = 8f;
    private float searchFocusDuration = 0.3f;
    private float pulseSpeed = 4f;
    private float pulseIntensity = 0.1f;
    private final Map<GroupedSettings, Float> categoryHoverAnimations = new HashMap<>();
    private final Map<GroupedSettings, Float> categoryActiveAnimations = new HashMap<>();
    private final Map<Setting<?, ?>, Float> settingHoverAnimations = new HashMap<>();
    private float searchFocusAnimation = 0f;
    private long lastFrameTime = System.currentTimeMillis();
    private final ImString searchText = new ImString();
    private boolean searchActive = false;

    public void setModule(Module module) {
        if (this.module != null) {
            awaitingWindowAdjustment = true;
        }

        this.module = module;
        this.config.moduleName.set(module.name, true);
        previewCategory = null;
        searchText.set("", true);
        searchActive = false;
    }

    @Override
    public String name() {
        return "module-config";
    }

    @Override
    public void show() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000f;
        lastFrameTime = currentTime;

        PersistentGuiSettings settings = Config.get().moduleConfigGui;

        if (module == null && !Objects.equals(config.moduleName.get(), "none")) {
            module = Psyop.MODULES.get(config.moduleName.get());
            assert module != null;
        }

        if (module == null
                || (module.getGroupedSettings().size() == 1
                && !module.getGroupedSettings().get(0).get().hasNext())) return;

        String windowTitle = StringUtils.readable(module.name, Config.get().coreSettings) + " Settings";
        if (ImGui.begin(windowTitle, state())) {
            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(500, 400, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(
                        ImGui.getMainViewport().getPosX() + 100,
                        ImGui.getMainViewport().getPosY() + 100,
                        ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            if (awaitingWindowAdjustment) {
                ImGui.setWindowSize(settings.width, settings.height);
                ImGui.setWindowPos(settings.x, settings.y);
                awaitingWindowAdjustment = false;
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, itemSpacing, verticalSpacing);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, frameRounding);
            ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, grabRounding);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, windowPaddingX, windowPaddingY);

            if (showSearchBar) {
                boolean searchFocused = ImGui.isItemFocused();
                searchFocusAnimation = AnimationUtils.animate(currentTime,
                        searchFocused ? currentTime : currentTime - (long) (searchFocusDuration * 1000),
                        searchFocusDuration,
                        searchFocused ? AnimationUtils.CUBIC_OUT : AnimationUtils.CUBIC_IN);

                ImGui.pushStyleColor(ImGuiCol.FrameBg,
                        AnimationUtils.lerpColor(
                                ImGui.getColorU32(ImGuiCol.FrameBg),
                                ImGui.getColorU32(ImGuiCol.FrameBgActive),
                                searchFocusAnimation
                        )
                );

                ImGui.pushItemWidth(-1);
                if (ImGui.inputTextWithHint("##settings_search", "Search settings...", searchText, ImGuiInputTextFlags.AutoSelectAll)) {
                    searchActive = !searchText.isEmpty();
                }
                ImGui.popItemWidth();
                ImGui.popStyleColor();

                ImGui.spacing();
            }

            ImGui.beginChild("settings_categories", sidebarWidth, 0, true);

            if (showCategoryHeaders) {
                ImGui.text("Categories");
                ImGui.separator();
                ImGui.spacing();
            }

            for (GroupedSettings group : module.getGroupedSettings()) {
                if (previewCategory == null) {
                    previewCategory = group;
                }

                boolean isSelected = group == previewCategory;
                float categoryHover = categoryHoverAnimations.getOrDefault(group, 0f);
                float categoryActive = categoryActiveAnimations.getOrDefault(group, 0f);
                boolean isCategoryHovered = false;

                categoryActive = AnimationUtils.lerp(
                        categoryActive,
                        isSelected ? 1f : 0f,
                        animationSpeed * deltaTime
                );
                categoryActiveAnimations.put(group, categoryActive);

                ImGui.pushID(group.getLabel());

                String displayName = group.getLabel();
                if (truncateLongCategoryNames && displayName.length() > maxCategoryNameLength) {
                    displayName = displayName.substring(0, maxCategoryNameLength) + "...";
                }

                ImVec4 bgColor = ImGui.getStyle().getColor(ImGuiCol.Button);
                ImVec4 hoverColor = ImGui.getStyle().getColor(ImGuiCol.ButtonHovered);
                ImVec4 activeColor = ImGui.getStyle().getColor(ImGuiCol.ButtonActive);

                if (categoryActive > 0) {
                    bgColor.x = AnimationUtils.lerp(bgColor.x, activeColor.x, categoryActive);
                    bgColor.y = AnimationUtils.lerp(bgColor.y, activeColor.y, categoryActive);
                    bgColor.z = AnimationUtils.lerp(bgColor.z, activeColor.z, categoryActive);
                }

                if (categoryHover > 0) {
                    bgColor.x = AnimationUtils.lerp(bgColor.x, hoverColor.x, categoryHover);
                    bgColor.y = AnimationUtils.lerp(bgColor.y, hoverColor.y, categoryHover);
                    bgColor.z = AnimationUtils.lerp(bgColor.z, hoverColor.z, categoryHover);
                }

                ImGui.pushStyleColor(ImGuiCol.Button, bgColor.x, bgColor.y, bgColor.z, bgColor.w);
                ImGui.pushStyleColor(ImGuiCol.Text, 0.90f, 0.90f, 0.95f, 1.00f);

                if (ImGui.button(displayName, -1, buttonHeight)) {
                    previewCategory = group;
                    searchActive = false;
                    searchText.set("", true);
                }

                isCategoryHovered = ImGui.isItemHovered();
                categoryHover = AnimationUtils.lerp(
                        categoryHover,
                        isCategoryHovered ? 1f : 0f,
                        animationSpeed * deltaTime
                );
                categoryHoverAnimations.put(group, categoryHover);

                ImGui.popStyleColor(2);

                if (ImGui.isItemHovered() && group.description() != null) {
                    ImGui.beginTooltip();
                    ImGui.text(group.description());
                    ImGui.endTooltip();
                }

                ImGui.popID();
                ImGui.spacing();
            }

            ImGui.endChild();

            ImGui.sameLine();

            ImGui.beginChild("settings_content", 0, 0, true);

            if (previewCategory != null) {
                if (showCategoryHeaders) {
                    ImGui.text(previewCategory.getLabel());
                    if (previewCategory.description() != null && !previewCategory.description().isBlank()) {
                        ImGui.sameLine();
                        ImGui.textDisabled("(?)");
                        if (ImGui.isItemHovered()) {
                            ImGui.beginTooltip();
                            ImGui.text(previewCategory.description());
                            ImGui.endTooltip();
                        }
                    }

                    ImGui.separator();
                    ImGui.spacing();
                }

                List<Setting<?, ?>> visibleSettings = new ArrayList<>();
                List<Setting<?, ?>> nameSearchSW = new ArrayList<>();
                List<Setting<?, ?>> nameSearch = new ArrayList<>();
                List<Setting<?, ?>> descriptionSearch = new ArrayList<>();

                String st = searchText.get().toLowerCase();

                for (Setting<?, ?> setting : previewCategory.getRaw()) {
                    if (setting.isVisible()) {
                        String sn = setting.label().toLowerCase();

                        if (!searchActive) {
                            visibleSettings.add(setting);
                        } else if (sn.startsWith(st)) {
                            nameSearchSW.add(setting);
                        } else if (sn.contains(st)) {
                            nameSearch.add(setting);
                        } else if (setting.description != null && !setting.description.isBlank() && setting.description.toLowerCase().contains(st)) {
                            descriptionSearch.add(setting);
                        }
                    }
                }

                visibleSettings.addAll(nameSearchSW);
                visibleSettings.addAll(nameSearch);
                visibleSettings.addAll(descriptionSearch);

                if (visibleSettings.isEmpty()) {
                    if (searchActive) {
                        ImGui.textColored(0.7f, 0.7f, 0.7f, 1.0f,
                                "No settings found matching: " + searchText.get());
                    } else {
                        ImGui.textColored(0.7f, 0.7f, 0.7f, 1.0f,
                                "No settings available in this category");
                    }
                } else {
                    int pos = 0;

                    for (Setting<?, ?> setting : visibleSettings) {
                        float settingHover = settingHoverAnimations.getOrDefault(setting, 0f);
                        boolean isSettingHovered;

                        ImGui.pushID(setting.name);
                        ImGui.beginGroup();

                        if (searchActive && setting.name.toLowerCase().contains(searchText.get().toLowerCase())) {
                            ImGui.pushStyleColor(ImGuiCol.Text,
                                    AnimationUtils.lerpColor(
                                            ImGui.getColorU32(ImGuiCol.Text),
                                            ImGui.getColorU32(ImGuiCol.CheckMark),
                                            0.7f
                                    )
                            );
                            ImGui.text(setting.name);
                            ImGui.popStyleColor();
                        } else {
                            ImGui.text(setting.name);
                        }

                        if (setting.description != null && !setting.description.isBlank()) {
                            ImGui.sameLine();
                            ImGui.textDisabled("(?)");
                            if (ImGui.isItemHovered()) {
                                ImGui.beginTooltip();
                                ImGui.text(setting.description);
                                ImGui.endTooltip();
                            }
                        }

                        setting.render();

                        if (showResetButtons && setting.canReset()) {
                            float availableWidth = ImGui.getContentRegionAvailX();
                            float resetButtonWidth = ImGui.calcTextSize("Reset").x + ImGui.getStyle().getFramePaddingX() * 2;

                            ImGui.sameLine(availableWidth - resetButtonWidth);

                            boolean shouldPushStyle = setting.defaultValue != setting.value();
                            if (shouldPushStyle) {
                                float pulseValue = AnimationUtils.pulse(currentTime / 1000f, pulseSpeed);
                                ImGui.pushStyleColor(ImGuiCol.Button,
                                        AnimationUtils.lighten(ImGui.getColorU32(ImGuiCol.Button), pulseValue * pulseIntensity)
                                );
                            }

                            if (ImGui.smallButton("Reset")) {
                                Psyop.log("Resetting {} to defaults.", setting.name);
                                setting.resetToDefault();
                            }

                            if (shouldPushStyle) {
                                ImGui.popStyleColor();
                            }

                            if (ImGui.isItemHovered()) {
                                ImGui.beginTooltip();
                                ImGui.text("Reset to default value");
                                ImGui.endTooltip();
                            }
                        }

                        ImGui.endGroup();

                        isSettingHovered = ImGui.isItemHovered();
                        settingHover = AnimationUtils.lerp(
                                settingHover,
                                isSettingHovered ? 1f : 0f,
                                animationSpeed * deltaTime
                        );
                        settingHoverAnimations.put(setting, settingHover);

                        if (settingHover > 0 && hoverBackgroundAlpha > 0) {
                            ImVec4 hoverColor = ImGui.getStyle().getColor(ImGuiCol.ButtonHovered);
                            GuiUtils.drawRectFilled(
                                    ImGui.getItemRectMin().minus(4, 2),
                                    ImGui.getItemRectMax().plus(4, 2),
                                    AnimationUtils.withAlpha(ImGui.getColorU32(hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w),
                                            settingHover * hoverBackgroundAlpha),
                                    hoverBackgroundRounding
                            );
                        }

                        ImGui.popID();

                        pos++;
                        if (pos < visibleSettings.size()) {
                            ImGui.spacing();
                            ImGui.separator();
                            ImGui.spacing();
                        }
                    }
                }
            }

            ImGui.endChild();

            ImGui.popStyleVar(4);
        }

        settings.x = ImGui.getWindowPosX();
        settings.y = ImGui.getWindowPosY();
        settings.width = ImGui.getWindowWidth();
        settings.height = ImGui.getWindowHeight();
        ImGui.end();
    }

    @Override
    public void populateSettings(Config config) {
        settings = config.moduleConfigGui;
        this.config = config.moduleConfigSettings;
    }
}