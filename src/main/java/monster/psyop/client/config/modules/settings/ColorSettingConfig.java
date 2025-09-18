package monster.psyop.client.config.modules.settings;

import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ColorSettingConfig extends SettingConfig<List<Float>> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        ColorSetting colorSetting = (ColorSetting) setting;

        this.recordedValue = new ArrayList<>();

        this.recordedValue.add(colorSetting.value()[0]);
        this.recordedValue.add(colorSetting.value()[1]);
        this.recordedValue.add(colorSetting.value()[2]);
        this.recordedValue.add(colorSetting.value()[3]);
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        ColorSetting colorSetting = (ColorSetting) setting;

        float[] mergedArray = new float[4];

        mergedArray[0] = (float) (double) (Object) recordedValue.get(0);
        mergedArray[1] = (float) (double) (Object) recordedValue.get(1);
        mergedArray[2] = (float) (double) (Object) recordedValue.get(2);
        mergedArray[3] = (float) (double) (Object) recordedValue.get(3);

        colorSetting.value(mergedArray);
    }
}
