package monster.psyop.client.config.modules.settings;

import imgui.type.ImString;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.types.StringListSetting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StringListSettingConfig extends SettingConfig<List<String>> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        StringListSetting stringListSetting = (StringListSetting) setting;
        this.recordedValue = new ArrayList<>();
        for (ImString v : stringListSetting.value()) {
            this.recordedValue.add(v.get());
        }
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        StringListSetting stringListSetting = (StringListSetting) setting;
        stringListSetting.value().clear();
        for (String v : recordedValue) {
            stringListSetting.value().add(new ImString(v));
        }
    }
}
