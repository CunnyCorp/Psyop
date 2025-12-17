package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.type.ImBoolean;
import monster.psyop.client.config.modules.settings.BoolSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;

public class ActionSetting extends Setting<ActionSetting, ImBoolean> {
    final transient private Runnable action;

    public ActionSetting(Builder builder) {
        super(builder);
        this.settingConfig = new BoolSettingConfig();
        this.action = builder.action;
    }

    public boolean get() {
        return value().get();
    }

    @Override
    public void render() {
        ImGui.button(this.label());

        if (ImGui.isItemClicked()) {
            action.run();
        }
    }

    public static class Builder extends SettingBuilder<ActionSetting, Builder, ImBoolean> {
        private Runnable action;

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        @Override
        public ActionSetting build() {
            this.defaultTo(new ImBoolean(false));

            if (this.action == null) {
                throw new RuntimeException("action is required when building a action setting.");
            }

            check();
            return new ActionSetting(this);
        }
    }
}
