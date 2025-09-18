package monster.psyop.client.framework.gui.views.client;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTableFlags;
import monster.psyop.client.Liberty;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.framework.gui.views.ViewHandler;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;

import java.util.Iterator;

public class ModulesViewRe extends View {
    private Category previewCategory = Categories.MISC;

    @Override
    public String name() {
        return "modules-re";
    }

    public void show() {
        PersistentGuiSettings settings = Config.get().modulesGui;

        if (ImGui.begin(displayName(), state())) {
            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(400, 600, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(0, 0, ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            ImGui.beginTable("##categories", 1, ImGuiTableFlags.Borders | ImGuiTableFlags.SizingFixedFit);
            for (Category category : Categories.INDEX) {
                if (Liberty.MODULES.getModules(category).isEmpty()) continue;

                ImGui.selectable(category.getLabel(), previewCategory == category);

                if (ImGui.isItemHovered()) {
                    previewCategory = category;

                    if (previewCategory.description != null) {
                        ImGui.beginTooltip();
                        ImGui.text(previewCategory.description);
                        ImGui.endTooltip();
                    }
                }

                ImGui.tableNextColumn();
            }
            ImGui.endTable();

            if (ImGui.beginChild(previewCategory.name + ":preview")) {
                for (Iterator<Module> it = Liberty.MODULES.getModules(previewCategory).listIterator();
                     it.hasNext(); ) {
                    Module module = it.next();

                    if (ImGui.checkbox(module.getLabel(), module.active)) {
                        module.toggled();
                    }

                    if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                        ViewHandler.get(ModuleConfigView.class).setModule(module);
                        ViewHandler.get(ModuleConfigView.class).state().set(true);
                    }

                    if (ImGui.isItemHovered() && module.description() != null) {
                        ImGui.beginTooltip();
                        ImGui.text(module.description());
                        ImGui.endTooltip();
                    }

                    if (it.hasNext()) {
                        ImGui.separator();
                    }
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
        settings = config.modulesGui;
    }
}
