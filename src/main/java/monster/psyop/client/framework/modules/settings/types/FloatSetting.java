package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.type.ImFloat;
import monster.psyop.client.config.modules.settings.FloatSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.ranges.FloatRange;

public class FloatSetting extends Setting<FloatSetting, ImFloat> {
    private final transient FloatRange range;

    public FloatSetting(Builder builder) {
        super(builder);
        this.range = builder.range;
        this.settingConfig = new FloatSettingConfig();
    }

    public float get() {
        return value().get();
    }

    @Override
    public ImFloat value(ImFloat value) {
        return super.value(value);
    }

    @Override
    public void render() {
        ImGui.sliderFloat("##" + label(), value().getData(), range.min(), range.max());
    }

    public static class Builder extends SettingBuilder<FloatSetting, Builder, ImFloat> {
        private FloatRange range = new FloatRange(0, 10);

        public Builder defaultTo(float v) {
            return super.defaultTo(new ImFloat(v));
        }

        public Builder range(float min, float max) {
            this.range = new FloatRange(min, max);
            return this;
        }

        @Override
        public FloatSetting build() {
            check();
            return new FloatSetting(this);
        }
    }
}
