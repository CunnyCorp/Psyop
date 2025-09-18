package monster.psyop.client.config.modules.settings;

import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.types.ObjectColorListSetting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListColorSettingConfig extends SettingConfig<List<String>> {
    public Map<String, float[]> colors = new HashMap<>();

    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        ObjectColorListSetting<?, S> objectListSetting = (ObjectColorListSetting<?, S>) setting;
        this.recordedValue = new ArrayList<>();
        for (S v : objectListSetting.value()) {
            this.recordedValue.add(objectListSetting.itemToString(v) + "=" + objectListSetting.colorMap.get(v)[0] + ";" + objectListSetting.colorMap.get(v)[1] + ";" + objectListSetting.colorMap.get(v)[2]);
            colors.put(objectListSetting.itemToString(v), objectListSetting.colorMap.get(v));
        }
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        ObjectColorListSetting<?, S> objectListSetting = (ObjectColorListSetting<?, S>) setting;

        objectListSetting.value().clear();

        for (String v : this.recordedValue) {
            String[] parts = v.split("=");
            objectListSetting.value().add(objectListSetting.parseItem(parts[0]));
            if (parts.length == 2) {
                float[] color = new float[]{0, 0, 0, 0};

                String[] floats = parts[1].split(";");

                int i = 0;

                for (var fl : floats) {
                    try {
                        color[i] = Float.parseFloat(fl);
                    } catch (NumberFormatException exception) {
                        break;
                    }
                    i++;
                }

                objectListSetting.colorMap.put(objectListSetting.parseItem(parts[0]), i == 3 ? color : new float[]{0, 0, 0, 1});
            }
        }
    }
}
