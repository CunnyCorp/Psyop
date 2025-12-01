package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.type.ImString;
import monster.psyop.client.config.modules.settings.StringSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;

public class StringSetting extends Setting<StringSetting, ImString> {

    public StringSetting(Builder builder) {
        super(builder);
        this.settingConfig = new StringSettingConfig();
    }

    public String get() {
        return value().get();
    }

    @Override
    public void render() {
        // Set the width for the next item only. This is safer than push/pop.
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() * 0.5f);
        ImGui.inputText("##" + name, value());
    }

    public static class Builder extends SettingBuilder<StringSetting, Builder, ImString> {
        public Builder defaultTo(String v) {
            return super.defaultTo(new ImString(v));
        }

        @Override
        public StringSetting build() {
            check();
            return new StringSetting(this);
        }
    }
}
