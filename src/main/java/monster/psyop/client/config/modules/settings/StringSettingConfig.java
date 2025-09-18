package monster.psyop.client.config.modules.settings;

import imgui.type.ImString;
import monster.psyop.client.framework.modules.settings.Setting;
import org.jetbrains.annotations.NotNull;

public class StringSettingConfig extends SettingConfig<String> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        this.recordedValue = ((ImString) setting.value()).get();
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        setting.setGenericValue(new ImString(this.recordedValue));
    }
}
