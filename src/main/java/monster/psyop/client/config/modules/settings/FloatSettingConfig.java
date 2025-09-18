package monster.psyop.client.config.modules.settings;

import imgui.type.ImFloat;
import monster.psyop.client.framework.modules.settings.Setting;
import org.jetbrains.annotations.NotNull;

public class FloatSettingConfig extends SettingConfig<Float> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        this.recordedValue = ((ImFloat) setting.value()).get();
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        setting.value((S) new ImFloat((float) (double) (Object) this.recordedValue));
    }
}
