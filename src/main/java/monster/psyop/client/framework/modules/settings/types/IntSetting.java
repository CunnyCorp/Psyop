package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.type.ImInt;
import monster.psyop.client.config.modules.settings.IntSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.ranges.IntRange;

public class IntSetting extends Setting<IntSetting, ImInt> {
    private final transient IntRange range;

    public IntSetting(Builder builder) {
        super(builder);
        this.range = builder.range;
        this.settingConfig = new IntSettingConfig();
    }

    public int get() {
        return value().get();
    }

    @Override
    public ImInt value(ImInt value) {
        return super.value(value);
    }

    public ImInt value(int i) {
        value().set(i);
        return value();
    }

    @Override
    public void render() {
        ImGui.sliderInt("##" + label(), value().getData(), range.min(), range.max());
    }

    public static class Builder extends SettingBuilder<IntSetting, Builder, ImInt> {
        private IntRange range = new IntRange(0, 10);

        public Builder defaultTo(int v) {
            return super.defaultTo(new ImInt(v));
        }

        public Builder range(int min, int max) {
            this.range = new IntRange(min, max);
            return this;
        }

        @Override
        public IntSetting build() {
            check();
            return new IntSetting(this);
        }
    }
}
