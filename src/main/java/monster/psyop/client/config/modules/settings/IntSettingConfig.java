package monster.psyop.client.config.modules.settings;

import imgui.type.ImInt;
import monster.psyop.client.framework.modules.settings.Setting;
import org.jetbrains.annotations.NotNull;

public class IntSettingConfig extends SettingConfig<Integer> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        this.recordedValue = ((ImInt) setting.value()).get();
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        setting.value((S) new ImInt((int) (double) (Object) this.recordedValue));
    }
}
