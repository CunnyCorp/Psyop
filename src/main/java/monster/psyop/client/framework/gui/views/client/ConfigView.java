package monster.psyop.client.framework.gui.views.client;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import monster.psyop.client.Liberty;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.CoreConfig;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.gui.Gui;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.framework.modules.settings.StringOptionSetting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.KeybindingSetting;
import monster.psyop.client.impl.events.game.OnKeyInput;
import monster.psyop.client.utility.StringUtils;

import java.util.*;
import java.util.function.Consumer;

public class ConfigView extends View {
    public CoreConfig config;

    private static final Map<String, List<SettingEntry>> settingsCategories = new LinkedHashMap<>();
    private static final List<Consumer<ConfigView>> initializationCallbacks = new ArrayList<>();

    public static KeybindingSetting guiKey;
    public static BoolSetting catSnap;
    public static BoolSetting chatFeedback;

    public static StringOptionSetting textCaseStyle;
    public static BoolSetting useSnakeCase;
    public static BoolSetting useCamelCase;
    public static BoolSetting useKebabCase;
    public static BoolSetting usePascalCase;

    static {
        registerInitializer(configView -> {
            guiKey = new KeybindingSetting.Builder()
                    .action(() -> {
                    })
                    .name("gui-bind")
                    .description("The key to bind for the gui.")
                    .defaultTo(new ImInt(56))
                    .build();

            catSnap = new BoolSetting.Builder()
                    .name("cat-snap")
                    .description("Swaps to categories based on if they're hovered.")
                    .defaultTo(new ImBoolean(false))
                    .build();

            chatFeedback = new BoolSetting.Builder()
                    .name("chat-feedback")
                    .description("Sends chat messages in-game. (Clientside)")
                    .defaultTo(new ImBoolean(false))
                    .build();

            textCaseStyle = new StringOptionSetting.Builder()
                    .name("text-case-style")
                    .description("How text should be formatted throughout the client")
                    .options(StringUtils.CASE_STYLES)
                    .defaultTo(new imgui.type.ImString("original"))
                    .build();

            useSnakeCase = new BoolSetting.Builder()
                    .name("use-snake-case")
                    .description("Convert spaces to underscores (snake_case)")
                    .defaultTo(new ImBoolean(false))
                    .build();

            useCamelCase = new BoolSetting.Builder()
                    .name("use-camel-case")
                    .description("Use camelCase formatting")
                    .defaultTo(new ImBoolean(false))
                    .build();

            useKebabCase = new BoolSetting.Builder()
                    .name("use-kebab-case")
                    .description("Convert spaces to hyphens (kebab-case)")
                    .defaultTo(new ImBoolean(false))
                    .build();

            usePascalCase = new BoolSetting.Builder()
                    .name("use-pascal-case")
                    .description("Use PascalCase formatting")
                    .defaultTo(new ImBoolean(false))
                    .build();

            configView.addSetting("Gui", "Interface", guiKey);
            configView.addSetting("Gui", "Interface", catSnap);
            configView.addSetting("Basic", "General", chatFeedback);
            configView.addSetting("Text", "Formatting", textCaseStyle);
            configView.addSetting("Text", "Formatting", useSnakeCase);
            configView.addSetting("Text", "Formatting", useCamelCase);
            configView.addSetting("Text", "Formatting", useKebabCase);
            configView.addSetting("Text", "Formatting", usePascalCase);
        });
    }

    // Helper class for organizing settings
    private record SettingEntry(String subcategory, Object setting) {
    }

    /**
     * Register an initialization callback for adding settings
     */
    public static void registerInitializer(Consumer<ConfigView> initializer) {
        initializationCallbacks.add(initializer);
    }

    /**
     * Add a setting to a specific category and subcategory
     */
    public void addSetting(String category, String subcategory, Object setting) {
        String key = category + "|" + subcategory;
        settingsCategories.computeIfAbsent(key, k -> new ArrayList<>()).add(new SettingEntry(subcategory, setting));
    }

    /**
     * Add a setting to a category with default subcategory
     */
    public void addSetting(String category, Object setting) {
        addSetting(category, "General", setting);
    }

    @Override
    public void load() {
        super.load();
        Liberty.EVENT_HANDLER.add(this);

        // Run all initialization callbacks
        initializationCallbacks.forEach(callback -> callback.accept(this));
    }

    @Override
    public String name() {
        return "config";
    }

    @EventListener(inGame = false)
    public void onKey(OnKeyInput event) {
        if (event.action != 1) {
            return;
        }

        if (guiKey.awaitingBinding && Gui.IS_LOADED.get() && state().get()) {
            if (!Objects.equals(KeyUtils.getTranslation(event.key), "none") && !Objects.equals(KeyUtils.getTranslation(event.key), "back")) {
                guiKey.value(new ImInt(event.key));
                config.guiBind.set(event.key);
            } else {
                guiKey.value(new ImInt(56));
                config.guiBind.set(56);
            }

            guiKey.awaitingBinding = false;
        } else if (guiKey.awaitingBinding) {
            guiKey.awaitingBinding = false;
        }
    }

    public void show() {
        if (ImGui.begin(displayName(), state())) {
            // Apply modern styling
            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 4.0f, 4.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 3.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 3.0f);

            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(450, 300, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(1358, 388, ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            // Track open tabs
            Map<String, Boolean> tabStates = new LinkedHashMap<>();

            if (ImGui.beginTabBar("categories")) {
                // Create tabs for each category
                Set<String> processedCategories = new HashSet<>();

                for (String categoryKey : settingsCategories.keySet()) {
                    String category = categoryKey.split("\\|")[0];

                    if (processedCategories.add(category)) {
                        boolean isOpen = ImGui.beginTabItem(category);
                        tabStates.put(category, isOpen);

                        if (isOpen) {
                            ImGui.endTabItem();
                        }
                    }
                }

                ImGui.endTabBar();
            }

            if (ImGui.beginChild("config_child", 0, 0, true)) {
                // Render content for each open tab
                for (Map.Entry<String, Boolean> entry : tabStates.entrySet()) {
                    if (entry.getValue()) {
                        String category = entry.getKey();

                        // Group settings by subcategory
                        Map<String, List<SettingEntry>> subcategoryMap = new LinkedHashMap<>();

                        for (Map.Entry<String, List<SettingEntry>> settingEntry : settingsCategories.entrySet()) {
                            if (settingEntry.getKey().startsWith(category + "|")) {
                                for (SettingEntry se : settingEntry.getValue()) {
                                    subcategoryMap.computeIfAbsent(se.subcategory, k -> new ArrayList<>()).add(se);
                                }
                            }
                        }

                        // Render each subcategory
                        for (Map.Entry<String, List<SettingEntry>> subcategoryEntry : subcategoryMap.entrySet()) {
                            if (subcategoryMap.size() > 1) {
                                ImGui.separator();
                                ImGui.text(subcategoryEntry.getKey());
                                ImGui.spacing();
                            }

                            // Render settings in this subcategory
                            for (SettingEntry settingEntry : subcategoryEntry.getValue()) {
                                if (settingEntry.setting instanceof BoolSetting setting) {
                                    ImGui.text(setting.label());
                                    setting.render();
                                } else if (settingEntry.setting instanceof KeybindingSetting) {
                                    ((KeybindingSetting) settingEntry.setting).render();
                                }
                                ImGui.spacing();
                            }
                        }
                    }
                }
            }

            ImGui.endChild();
            ImGui.popStyleVar(3);
        }

        settings.x = ImGui.getWindowPosX();
        settings.y = ImGui.getWindowPosY();
        settings.width = ImGui.getWindowWidth();
        settings.height = ImGui.getWindowHeight();
        ImGui.end();
    }

    @Override
    public void populateSettings(Config config) {
        this.settings = config.configGui;
        this.config = config.coreSettings;

        // Apply values from config to all registered settings
        for (List<SettingEntry> entries : settingsCategories.values()) {
            for (SettingEntry entry : entries) {
                if (entry.setting == guiKey) {
                    guiKey.value(config.coreSettings.guiBind);
                } else if (entry.setting == catSnap) {
                    catSnap.value(config.coreSettings.moduleCatSnap);
                } else if (entry.setting == chatFeedback) {
                    chatFeedback.value(config.coreSettings.chatFeedback);
                } else if (entry.setting == textCaseStyle) {
                    textCaseStyle.value(config.coreSettings.textCaseStyle);
                } else if (entry.setting == useSnakeCase) {
                    useSnakeCase.value(config.coreSettings.useSnakeCase);
                } else if (entry.setting == useCamelCase) {
                    useCamelCase.value(config.coreSettings.useCamelCase);
                } else if (entry.setting == useKebabCase) {
                    useKebabCase.value(config.coreSettings.useKebabCase);
                } else if (entry.setting == usePascalCase) {
                    usePascalCase.value(config.coreSettings.usePascalCase);
                }
            }
        }
    }

    // Update the populateConfig method
    public static void populateConfig() {
        Config.get().coreSettings.chatFeedback.set(chatFeedback.get());
        Config.get().coreSettings.moduleCatSnap.set(catSnap.get());

        // Populate string formatting settings
        Config.get().coreSettings.textCaseStyle.set(textCaseStyle.value().get(), true);
        Config.get().coreSettings.useSnakeCase.set(useSnakeCase.get());
        Config.get().coreSettings.useCamelCase.set(useCamelCase.get());
        Config.get().coreSettings.useKebabCase.set(useKebabCase.get());
        Config.get().coreSettings.usePascalCase.set(usePascalCase.get());
    }
}