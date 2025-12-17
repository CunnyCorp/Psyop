package monster.psyop.client.framework.gui.views.client;

import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImString;
import monster.psyop.client.Psyop;
import monster.psyop.client.config.Config;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.framework.gui.views.ViewHandler;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.modules.client.GUIModule;

import java.util.ArrayList;
import java.util.List;

public class ModulesView extends View {
    private Category previewCategory = Categories.COMBAT;
    private final ImString searchText = new ImString("", 256);

    public ModulesView() {
    }

    @Override
    public String name() {
        return "modules";
    }

    @Override
    public void show() {
        float[] tooltipColor = GUIModule.INSTANCE.tooltipColor.get();

        if (ImGui.begin(displayName(), state(), ImGuiWindowFlags.AlwaysUseWindowPadding)) {
            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(578, 400, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(60, 60, ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            ImGui.inputTextWithHint("##modules_search", "Search for modules...", searchText);

            ImGui.textDisabled("Right click modules to configure them.");

            if (ImGui.beginTabBar("modules_categories", ImGuiTabBarFlags.NoCloseWithMiddleMouseButton)) {
                for (Category cat : Categories.INDEX) {
                    boolean tabSelected = ImGui.beginTabItem(cat.getLabel());

                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.textColored(tooltipColor[0], tooltipColor[1], tooltipColor[2], tooltipColor[3], cat.description);
                        ImGui.endTooltip();
                    }

                    if (tabSelected) {
                        previewCategory = cat;
                        ImGui.endTabItem();
                    }
                }

                ImGui.endTabBar();
            }


            ImGui.beginChild("modules_layout", 0, 0, true);

            List<Module> modules = new ArrayList<>();
            List<Module> nameSearchSW = new ArrayList<>();
            List<Module> nameSearch = new ArrayList<>();
            List<Module> descriptionSearch = new ArrayList<>();
            List<Module> settingSearch = new ArrayList<>();

            if (searchText.isNotEmpty()) {
                String st = searchText.get().toLowerCase();

                for (Category cat : Categories.INDEX) {
                    for (Module module : Psyop.MODULES.getModules(cat)) {
                        if (!module.shouldRender()) continue;

                        boolean matchesNameSW = module.getLabel().toLowerCase().startsWith(st);
                        boolean matchesName = module.getLabel().toLowerCase().contains(st);
                        boolean matchesDescription = module.description() != null && module.description().toLowerCase().contains(st);
                        boolean matchesSetting = false;

                        // Search for setting names!
                        if (st.length() >= 3) {
                            for (var groups : module.getGroupedSettings()) {
                                for (var setting : groups.getRaw()) {
                                    if (setting.label().toLowerCase().contains(st)) {
                                        matchesSetting = true;
                                    }
                                }
                            }
                        }

                        if (matchesNameSW) {
                            nameSearchSW.add(module);
                        } else if (matchesName) {
                            nameSearch.add(module);
                        } else if (matchesDescription) {
                            descriptionSearch.add(module);
                        } else if (matchesSetting) {
                            settingSearch.add(module);
                        }
                    }
                }

                modules.addAll(nameSearchSW);
                modules.addAll(nameSearch);
                modules.addAll(descriptionSearch);
                modules.addAll(settingSearch);
            } else {
                modules.addAll(Psyop.MODULES.getModules(previewCategory));
            }

            if (ImGui.beginTable("modules_table", 3)) {

                ImGui.tableNextRow();

                for (Module module : modules) {
                    if (!module.shouldRender()) {
                        continue;
                    }

                    ImGui.tableNextColumn();

                    String buttonText = module.getLabel();
                    String keybindText = KeyUtils.getTranslation(module.keybinding.value().get());

                    if (!keybindText.isEmpty() && !keybindText.equals("none")) {
                        ImGui.textDisabled("[" + keybindText + "]");
                        ImGui.sameLine();
                    }

                    if (module.active()) {
                        ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgba(ImGui.getStyle().getColor(ImGuiCol.ButtonActive)));
                    }

                    ImGui.button(buttonText, -1, 24);

                    if (module.active()) {
                        ImGui.popStyleColor();
                    }

                    if (ImGui.isItemClicked()) {
                        module.active(!module.active());
                    }

                    if (ImGui.isItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                        ModuleConfigView configView = ViewHandler.get(ModuleConfigView.class);
                        configView.setModule(module);
                        configView.state().set(true);
                    }

                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.textColored(tooltipColor[0], tooltipColor[1], tooltipColor[2], tooltipColor[3], module.description());
                        ImGui.endTooltip();
                    }
                }

                ImGui.endTable();
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
        this.settings = config.modulesGui;

        if (!settings.hasLoaded) {
            settings.isLoaded.set(true);
        }
    }
}