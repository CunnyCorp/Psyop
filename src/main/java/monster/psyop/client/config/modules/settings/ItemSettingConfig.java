package monster.psyop.client.config.modules.settings;

import monster.psyop.client.framework.modules.settings.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class ItemSettingConfig extends SettingConfig<String> {
    @Override
    public <S> void fromSetting(@NotNull Setting<?, S> setting) {
        this.recordedValue = BuiltInRegistries.ITEM.getKey(((Item) setting.value())).toString();
    }

    @Override
    public <S> void populateSetting(@NotNull Setting<?, S> setting) {
        setting.setGenericValue(BuiltInRegistries.ITEM.get(ResourceLocation.parse(this.recordedValue)));
    }
}
