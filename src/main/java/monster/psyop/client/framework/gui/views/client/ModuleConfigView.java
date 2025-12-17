package monster.psyop.client.framework.gui.views.client;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTabBarFlags;
import imgui.type.ImString;
import lombok.Getter;
import monster.psyop.client.Psyop;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.ModuleConfigSettings;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.impl.modules.client.GUIModule;
import monster.psyop.client.utility.AnimationUtils;
import monster.psyop.client.utility.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModuleConfigView extends View {
    public ModuleConfigSettings config;
    @Getter
    private Module module;
    private boolean awaitingWindowAdjustment;
    private GroupedSettings previewCategory;
    private final ImString searchText = new ImString("", 256);
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
        float[] tooltipColor = GUIModule.INSTANCE.tooltipColor.get();

        PersistentGuiSettings settings = Config.get().moduleConfigGui;

        if (module == null && !Objects.equals(config.moduleName.get(), "none")) {
            module = Psyop.MODULES.get(config.moduleName.get());
            assert module != null;
        }

        if (module == null
                || (module.getGroupedSettings().size() == 1
                && !module.getGroupedSettings().get(0).get().hasNext())) return;

        String windowTitle = StringUtils.readable(module.name) + " Settings";
        if (ImGui.begin(windowTitle, state())) {
            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(138, 491, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(
                        500,
                        404,
                        ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            if (awaitingWindowAdjustment) {
                ImGui.setWindowSize(settings.width, settings.height);
                ImGui.setWindowPos(settings.x, settings.y);
                awaitingWindowAdjustment = false;
            }

            if (ImGui.inputTextWithHint("##settings_search", "Search settings...", searchText, ImGuiInputTextFlags.AutoSelectAll)) {
                searchActive = !searchText.isEmpty();
            }

            ImGui.spacing();

            if (ImGui.beginTabBar("config_categories", ImGuiTabBarFlags.NoCloseWithMiddleMouseButton)) {
                for (GroupedSettings cat : module.getGroupedSettings()) {
                    boolean tabSelected = ImGui.beginTabItem(cat.getLabel());

                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.textColored(tooltipColor[0], tooltipColor[1], tooltipColor[2], tooltipColor[3], cat.description());
                        ImGui.endTooltip();
                    }

                    if (tabSelected) {
                        previewCategory = cat;
                        ImGui.endTabItem();
                    }
                }

                ImGui.endTabBar();
            }

            ImGui.separator();

            ImGui.beginChild("settings_content", 0, 0, true);

            if (previewCategory != null) {
                ImGui.text(previewCategory.getLabel());
                if (previewCategory.description() != null && !previewCategory.description().isBlank()) {
                    ImGui.sameLine();
                    ImGui.textDisabled("(?)");
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.textColored(tooltipColor[0], tooltipColor[1], tooltipColor[2], tooltipColor[3], previewCategory.description());
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

            int pos = 0;

            for (Setting<?, ?> setting : visibleSettings) {
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
                        ImGui.textColored(tooltipColor[0], tooltipColor[1], tooltipColor[2], tooltipColor[3], setting.description);
                        ImGui.endTooltip();
                    }
                }

                setting.render();

                if (setting.canReset()) {
                    float availableWidth = ImGui.getContentRegionAvailX();
                    float resetButtonWidth = ImGui.calcTextSize("Reset").x + ImGui.getStyle().getFramePaddingX() * 2;

                    ImGui.sameLine(availableWidth - resetButtonWidth);

                    boolean shouldPushStyle = setting.defaultValue != setting.value();
                    if (shouldPushStyle) {
                        ImGui.pushStyleColor(ImGuiCol.Button, ImGui.getColorU32(ImGuiCol.ButtonActive));
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
                        ImGui.textColored(tooltipColor[0], tooltipColor[1], tooltipColor[2], tooltipColor[3], "Reset to default value");
                        ImGui.endTooltip();
                    }
                }

                ImGui.endGroup();

                ImGui.popID();

                pos++;
                if (pos < visibleSettings.size()) {
                    ImGui.spacing();
                    ImGui.separator();
                    ImGui.spacing();
                }
            }

            ImGui.endChild();
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