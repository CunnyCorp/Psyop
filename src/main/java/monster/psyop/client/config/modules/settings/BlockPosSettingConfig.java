package monster.psyop.client.config.modules.settings;

import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.wrappers.ImBlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BlockPosSettingConfig extends SettingConfig<ArrayList<Integer>> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        ImBlockPos pos = (ImBlockPos) setting.value();
        this.recordedValue = new ArrayList<>();

        this.recordedValue.add(pos.x());
        this.recordedValue.add(pos.y());
        this.recordedValue.add(pos.z());
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        setting.setGenericValue(new ImBlockPos((int) (double) (Object) recordedValue.get(0), (int) (double) (Object) recordedValue.get(1), (int) (double) (Object) recordedValue.get(2)));
    }
}
