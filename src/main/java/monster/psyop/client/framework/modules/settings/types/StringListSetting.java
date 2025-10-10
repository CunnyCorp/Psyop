package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImString;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.modules.settings.StringListSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;

import java.util.List;

public class StringListSetting extends Setting<StringListSetting, List<ImString>> {
    private final ImString txt = new ImString(256);

    public StringListSetting(SettingBuilder<StringListSetting, Builder, List<ImString>> builder) {
        super(builder);
        this.settingConfig = new StringListSettingConfig();
    }

    @Override
    public void render() {
        if (ImGui.checkbox("Hide", Config.get().isHidden(this))) {
            Config.get().hide(this, !Config.get().isHidden(this));
        }

        if (Config.get().isHidden(this)) {
            return;
        }

        ImGui.beginChild(
                name + "_child", ImGui.getWindowWidth() - 10, ImGui.getWindowHeight() / 2, true);

        if (ImGui.beginTable(name + "_table", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.SizingFixedFit)) {
            ImString removeEntry = null;

            int i = 0;
            for (ImString entry : value()) {
                i++;
                ImGui.tableNextColumn();

                ImGui.text(entry.get());

                ImGui.tableNextColumn();
                if (ImGui.button("Remove##" + i + "_" + name)) {
                    removeEntry = entry;
                }
            }

            if (removeEntry != null) {
                value().remove(removeEntry);
            }

            ImGui.endTable();
        }

        ImGui.endChild();

        boolean submit = ImGui.button("Submit##" + name + "_submit");

        ImGui.sameLine();

        ImGui.inputText("##Text" + name, txt, ImGuiInputTextFlags.CallbackResize);

        if (submit) {
            if (txt.isEmpty()) {
                return;
            }

            value().add(new ImString(txt.get()));
            // Avoid resizing the backing buffer in the same frame as inputText to prevent crashes
            txt.set("");
        }
    }

    public static class Builder
            extends Setting.SettingBuilder<StringListSetting, Builder, List<ImString>> {
        @Override
        public StringListSetting build() {
            check();
            return new StringListSetting(this);
        }
    }
}
