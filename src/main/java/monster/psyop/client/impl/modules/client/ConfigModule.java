package monster.psyop.client.impl.modules.client;

import imgui.type.ImInt;
import imgui.type.ImString;
import monster.psyop.client.config.Config;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.utility.PathIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigModule extends Module {
    public long lastCheckedTime = 0L;
    public boolean hasRefreshedConfig = true;
    public boolean renderOnly = false;
    public ArrayList<ImString> configFiles = new ArrayList<>(List.of(new ImString("config")));
    public static ConfigModule INSTANCE;
    public Path configsPath = PathIndex.CLIENT.resolve("configs");

    public KeybindingSetting openGui = new KeybindingSetting.Builder()
            .name("open-gui")
            .defaultTo(new ImInt(57 - 13))
            .addTo(coreGroup);
    public BoolSetting debug = new BoolSetting.Builder()
            .name("debug")
            .defaultTo(false)
            .addTo(coreGroup);
    public ProvidedStringSetting configs = new ProvidedStringSetting.Builder()
            .suggestions(configFiles)
            .name("configs")
            .defaultTo(new ImString("config"))
            .addTo(coreGroup);
    public ActionSetting loadConfig = new ActionSetting.Builder()
            .name("load-config")
            .action(() -> {
                hasRefreshedConfig = false;
                renderOnly = false;
            })
            .addTo(coreGroup);
    public ActionSetting loadRenders = new ActionSetting.Builder()
            .name("load-renders")
            .action(() -> {
                hasRefreshedConfig = false;
                renderOnly = true;
            })
            .addTo(coreGroup);
    public StringSetting currentConfigName = new StringSetting.Builder()
            .name("config-name")
            .defaultTo("default")
            .addTo(coreGroup);
    public ActionSetting saveConfig = new ActionSetting.Builder()
            .name("save-config")
            .action(() -> Config.get().save(configsPath.resolve(currentConfigName.get() + ".json")))
            .addTo(coreGroup);

    public ConfigModule() {
        super(Categories.CLIENT, "config", "Manage basic configuration options!");
        INSTANCE = this;
        try {
            Files.createDirectories(configsPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void active(boolean active) {
    }

    public void loadConfigs() {
        if (Files.exists(configsPath) && Files.isDirectory(configsPath)) {
            try {
                configFiles.clear();

                for (var file : Files.list(configsPath).toList()) {
                    String str = file.getFileName().toString();

                    if (str.endsWith(".json")) {
                        configFiles.add(new ImString(str.replace(".json", "")));
                    }
                }

                configs.suggestions = configFiles;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isDebugging() {
        return INSTANCE != null && INSTANCE.debug.get();
    }
}
