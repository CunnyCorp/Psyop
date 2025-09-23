package monster.psyop.client.framework.gui.views.client;

import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.*;
import imgui.type.ImString;
import monster.psyop.client.Psyop;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.framework.gui.views.ViewHandler;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.utility.AnimationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModulesView extends View {
    // Configuration variables
    private Category previewCategory = Categories.COMBAT;
    private final ImString searchText = new ImString();
    private boolean searchActive = false;
    private float categoriesPanelWidth = 140.0f;
    private boolean categoriesOnLeft = true;
    private boolean showCategoryHeaders = true;
    private boolean showKeybinds = true;
    private float buttonHeight = 0.0f; // 0 = auto
    private float itemSpacing = 6.0f;
    private float verticalSpacing = 4.0f;
    private float frameRounding = 3.0f;
    private float grabRounding = 3.0f;
    private float windowPaddingX = 8.0f;
    private float windowPaddingY = 8.0f;
    private float animationSpeed = 8f;
    private float searchFocusDuration = 0.3f;
    private boolean filterEmptyCategories = true;
    private boolean searchIncludesDescription = false;
    private final Map<Module, Float> moduleHoverAnimations = new HashMap<>();
    private final Map<Module, Float> moduleActiveAnimations = new HashMap<>();
    private final Map<Category, Float> categoryHoverAnimations = new HashMap<>();
    private final Map<Category, Float> categoryActiveAnimations = new HashMap<>();
    private long lastFrameTime = System.currentTimeMillis();

    @Override
    public String name() {
        return "modules";
    }

    public void show() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000f;
        lastFrameTime = currentTime;

        PersistentGuiSettings settings = Config.get().modulesGui;

        if (ImGui.begin(displayName(), state())) {
            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(700, 500, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(0, 0, ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, itemSpacing, verticalSpacing);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, frameRounding);
            ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, grabRounding);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, windowPaddingX, windowPaddingY);

            boolean searchFocused = ImGui.isItemFocused();
            float searchFocusAnimation = AnimationUtils.animate(currentTime,
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
            if (ImGui.inputTextWithHint("##search", "Search modules...", searchText, ImGuiInputTextFlags.AutoSelectAll)) {
                searchActive = !searchText.isEmpty();
            }
            ImGui.popItemWidth();
            ImGui.popStyleColor();

            ImGui.spacing();

            ImGui.beginChild("split_layout", 0, 0, false, ImGuiWindowFlags.NoScrollbar);

            if (categoriesOnLeft) {
                renderCategoriesPanel(deltaTime);
                ImGui.sameLine();
                renderModulesPanel(deltaTime);
            } else {
                renderModulesPanel(deltaTime);
                ImGui.sameLine();
                renderCategoriesPanel(deltaTime);
            }

            ImGui.endChild();

            ImGui.popStyleVar(4);
        }
        ImGui.end();

        settings.x = ImGui.getWindowPosX();
        settings.y = ImGui.getWindowPosY();
        settings.width = ImGui.getWindowWidth();
        settings.height = ImGui.getWindowHeight();
    }

    private void renderCategoriesPanel(float deltaTime) {
        ImGui.beginChild("categories_panel", categoriesPanelWidth, 0, true);

        if (showCategoryHeaders) {
            ImGui.text("Categories");
            ImGui.separator();
            ImGui.spacing();
        }

        for (Category category : Categories.INDEX) {
            if (filterEmptyCategories && Psyop.MODULES.getModules(category).isEmpty()) continue;

            float categoryHover = categoryHoverAnimations.getOrDefault(category, 0f);
            float categoryActive = categoryActiveAnimations.getOrDefault(category, 0f);
            boolean isCategoryHovered;
            boolean isSelected = category == previewCategory;

            categoryActive = AnimationUtils.lerp(
                    categoryActive,
                    isSelected ? 1f : 0f,
                    animationSpeed * deltaTime
            );
            categoryActiveAnimations.put(category, categoryActive);

            ImGui.pushID(category.name);

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

            if (ImGui.button(category.name, -1, buttonHeight)) {
                previewCategory = category;
            }

            isCategoryHovered = ImGui.isItemHovered();
            categoryHover = AnimationUtils.lerp(
                    categoryHover,
                    isCategoryHovered ? 1f : 0f,
                    animationSpeed * deltaTime
            );
            categoryHoverAnimations.put(category, categoryHover);

            ImGui.popStyleColor(2);
            ImGui.popID();

            ImGui.spacing();
        }

        ImGui.endChild();
    }

    private void renderModulesPanel(float deltaTime) {
        ImGui.beginChild("modules_panel", 0, 0, true);

        if (showCategoryHeaders) {
            ImGui.text(previewCategory.name);
            ImGui.separator();
            ImGui.spacing();
        }

        List<Module> modules = new ArrayList<>();
        if (searchActive) {
            for (Category cat : Categories.INDEX) {
                for (Module module : Psyop.MODULES.getModules(cat)) {
                    boolean matchesName = module.getLabel().toLowerCase().contains(searchText.get().toLowerCase());
                    boolean matchesDescription = searchIncludesDescription &&
                            module.description() != null &&
                            module.description().toLowerCase().contains(searchText.get().toLowerCase());

                    if (matchesName || matchesDescription) {
                        modules.add(module);
                    }
                }
            }
        } else {
            modules.addAll(Psyop.MODULES.getModules(previewCategory));
        }

        for (Module module : modules) {
            float moduleHover = moduleHoverAnimations.getOrDefault(module, 0f);
            float moduleActive = moduleActiveAnimations.getOrDefault(module, 0f);
            boolean isModuleHovered;

            moduleActive = AnimationUtils.lerp(
                    moduleActive,
                    module.active() ? 1f : 0f,
                    animationSpeed * deltaTime
            );
            moduleActiveAnimations.put(module, moduleActive);

            ImGui.pushID(module.name);

            ImVec4 bgColor = ImGui.getStyle().getColor(ImGuiCol.Button);
            ImVec4 hoverColor = ImGui.getStyle().getColor(ImGuiCol.ButtonHovered);
            ImVec4 activeColor = ImGui.getStyle().getColor(ImGuiCol.ButtonActive);

            if (moduleActive > 0) {
                bgColor.x = AnimationUtils.lerp(bgColor.x, activeColor.x, moduleActive);
                bgColor.y = AnimationUtils.lerp(bgColor.y, activeColor.y, moduleActive);
                bgColor.z = AnimationUtils.lerp(bgColor.z, activeColor.z, moduleActive);
            }

            if (moduleHover > 0) {
                bgColor.x = AnimationUtils.lerp(bgColor.x, hoverColor.x, moduleHover);
                bgColor.y = AnimationUtils.lerp(bgColor.y, hoverColor.y, moduleHover);
                bgColor.z = AnimationUtils.lerp(bgColor.z, hoverColor.z, moduleHover);
            }

            ImGui.pushStyleColor(ImGuiCol.Button, bgColor.x, bgColor.y, bgColor.z, bgColor.w);
            ImGui.pushStyleColor(ImGuiCol.Text, 0.90f, 0.90f, 0.95f, 1.00f);

            String buttonText = module.getLabel();
            if (showKeybinds) {
                String keybindText = KeyUtils.getTranslation(module.keybinding.value().get());
                if (!keybindText.isEmpty()) {
                    buttonText += " (" + keybindText + ")";
                }
            }

            if (ImGui.button(buttonText, -1, buttonHeight)) {
                module.active(!module.active());
            }

            if (ImGui.isItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                ModuleConfigView configView = ViewHandler.get(ModuleConfigView.class);
                configView.setModule(module);
                configView.state().set(true);
            }

            isModuleHovered = ImGui.isItemHovered();

            moduleHover = AnimationUtils.lerp(
                    moduleHover,
                    isModuleHovered ? 1f : 0f,
                    animationSpeed * deltaTime
            );
            moduleHoverAnimations.put(module, moduleHover);

            ImGui.popStyleColor(2);
            ImGui.popID();

            ImGui.spacing();
        }

        ImGui.endChild();
    }

    @Override
    public void populateSettings(Config config) {
        this.settings = config.modulesGui;
    }
}