package monster.psyop.client.impl.modules.client;

import imgui.type.ImInt;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.KeybindingSetting;

public class ConfigModule extends Module {
    public static ConfigModule INSTANCE;
    public KeybindingSetting openGui = new KeybindingSetting.Builder()
            .name("open-gui")
            .defaultTo(new ImInt(57 - 13))
            .addTo(coreGroup);
    public BoolSetting debug = new BoolSetting.Builder()
            .name("debug")
            .defaultTo(false)
            .addTo(coreGroup);

    public ConfigModule() {
        super(Categories.CLIENT, "config", "Manage basic configuration options!");
        INSTANCE = this;
    }

    public static boolean isDebugging() {
        return INSTANCE != null && INSTANCE.debug.get();
    }
}
