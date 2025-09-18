package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.type.ImBoolean;
import monster.psyop.client.config.modules.settings.BoolSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;

public class BoolSetting extends Setting<BoolSetting, ImBoolean> {
    public BoolSetting(Builder builder) {
        super(builder);
        this.settingConfig = new BoolSettingConfig();
    }

    public boolean get() {
        return value().get();
    }

    @Override
    public void render() {
        ImGui.checkbox("##" + label(), value());
    }

    public static class Builder extends SettingBuilder<BoolSetting, Builder, ImBoolean> {
        public Builder defaultTo(boolean v) {
            this.defaultTo(new ImBoolean(v));
            return this;
        }

        @Override
        public BoolSetting build() {
            check();
            return new BoolSetting(this);
        }
    }
}
