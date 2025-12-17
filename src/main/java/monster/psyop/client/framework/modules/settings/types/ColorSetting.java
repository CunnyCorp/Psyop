package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import monster.psyop.client.config.modules.settings.ColorSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import org.jetbrains.annotations.NotNull;

public class ColorSetting extends Setting<ColorSetting, float[]> {
    public ColorSetting(Builder builder) {
        super(builder);
        this.settingConfig = new ColorSettingConfig();
    }

    public float[] get() {
        return value();
    }

    public int getColor() {
        return ImColorW.packed(get());
    }

    @Override
    public float[] value(float[] value) {
        return super.value(value);
    }

    @Override
    public void render() {
        ImGui.colorEdit4("##" + this.label(), get(), ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.OptionsDefault);
    }

    public static class Builder extends SettingBuilder<ColorSetting, Builder, float[]> {
        public Builder defaultTo(float @NotNull [] v) {
            return super.defaultTo(v);
        }

        @Override
        public ColorSetting build() {
            check();
            return new ColorSetting(this);
        }
    }
}