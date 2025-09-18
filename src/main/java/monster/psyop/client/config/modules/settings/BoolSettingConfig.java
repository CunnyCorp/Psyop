package monster.psyop.client.config.modules.settings;

import imgui.type.ImBoolean;
import monster.psyop.client.framework.modules.settings.Setting;
import org.jetbrains.annotations.NotNull;

public class BoolSettingConfig extends SettingConfig<Boolean> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        this.recordedValue = ((ImBoolean) setting.value()).get();
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        setting.setGenericValue(new ImBoolean(recordedValue));
    }
}
