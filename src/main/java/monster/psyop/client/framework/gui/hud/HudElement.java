package monster.psyop.client.framework.gui.hud;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import monster.psyop.client.Liberty;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.framework.gui.Gui;
import monster.psyop.client.utility.StringUtils;

import static monster.psyop.client.Liberty.MC;

public abstract class HudElement {
    public PersistentGuiSettings settings = new PersistentGuiSettings();
    private String readableName;

    public abstract String name();

    public String displayName() {
        if (readableName == null) {
            readableName = StringUtils.readable(name(), Config.get().coreSettings);
        }

        String name = readableName;

        if (Config.get().guiSettings.consistentViews.contains(name())) {
            name += " (C)";
        }

        return name;
    }

    public int defaultWindowFlags() {
        int nonEditorFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar |
                ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoNavInputs |
                ImGuiWindowFlags.NoNav | ImGuiWindowFlags.NoNavFocus |
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoInputs;
        int editorFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar |
                ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoNavInputs |
                ImGuiWindowFlags.NoNav | ImGuiWindowFlags.NoNavFocus;
        return Gui.IS_LOADED.get() ? editorFlags : nonEditorFlags;
    }

    public abstract int defaultWidth();

    public abstract int defaultHeight();

    public abstract int defaultX();

    public abstract int defaultY();

    public void fillPosAndSize() {
        if (settings == null) {
            return;
        }

        if (settings.hasLoaded) {
            ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
            ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
            ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
            ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
        } else {
            ImGui.setWindowSize(defaultWidth(), defaultHeight(), ImGuiCond.FirstUseEver);
            ImGui.setWindowPos(
                    defaultX(),
                    defaultY(),
                    ImGuiCond.FirstUseEver);
            settings.hasLoaded = true;
        }
    }

    public void copyPosAndSize() {
        settings.x = ImGui.getWindowPosX();
        settings.y = ImGui.getWindowPosY();
        settings.width = ImGui.getWindowWidth();
        settings.height = ImGui.getWindowHeight();
    }

    public boolean shouldShow() {
        return state().get() && MC.getWindow().isFullscreen();
    }

    public abstract void show();

    public void populateSettings(Config config) {
        if (config.hudElements.containsKey(name())) {
            Liberty.log("Loading config for {} Hud Element.", name());
            this.settings = config.hudElements.get(name());
        } else {
            Liberty.warn("Hud Element {} has no config.", name());
        }
    }

    public ImBoolean state() {
        return settings.isLoaded;
    }

    public void load() {
        HudHandler.add(this);
    }
}
