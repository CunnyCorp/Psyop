package monster.psyop.client.config.modules.settings;

import monster.psyop.client.Liberty;
import monster.psyop.client.framework.modules.settings.Setting;
import org.jetbrains.annotations.NotNull;

public class SettingConfig<R> {
    public R recordedValue;

    public <S> void cloneTo(SettingConfig<S> settingConfig) {
        settingConfig.recordedValue = (S) recordedValue;
    }

    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        Liberty.LOG.info("Trying to load setting config for {} with prim.", setting.name);
    }

    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        Liberty.LOG.info("Trying to populate setting config for {} with prim.", setting.name);
    }
}
