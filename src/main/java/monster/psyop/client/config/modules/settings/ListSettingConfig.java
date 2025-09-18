package monster.psyop.client.config.modules.settings;

import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.types.ObjectListSetting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ListSettingConfig extends SettingConfig<List<String>> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        ObjectListSetting<?, S> objectListSetting = (ObjectListSetting<?, S>) setting;
        this.recordedValue = new ArrayList<>();
        for (S v : objectListSetting.value()) {
            this.recordedValue.add(objectListSetting.itemToString(v));
        }
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        ObjectListSetting<?, S> objectListSetting = (ObjectListSetting<?, S>) setting;

        objectListSetting.value().clear();

        for (String v : this.recordedValue) {
            objectListSetting.value().add(objectListSetting.parseItem(v));
        }
    }
}
