package monster.psyop.client.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import lombok.SneakyThrows;
import monster.psyop.client.Psyop;
import monster.psyop.client.config.adapters.*;
import monster.psyop.client.config.gui.*;
import monster.psyop.client.config.modules.ModuleConfig;
import monster.psyop.client.config.modules.SettingGroupConfig;
import monster.psyop.client.config.modules.settings.wraps.ObjectColor;
import monster.psyop.client.config.sub.Inventory;
import monster.psyop.client.config.sub.Placing;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.friends.RoleType;
import monster.psyop.client.framework.gui.hud.HudElement;
import monster.psyop.client.framework.gui.hud.HudHandler;
import monster.psyop.client.framework.gui.views.ViewHandler;
import monster.psyop.client.framework.gui.views.client.ConfigView;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.utility.FileSystem;
import monster.psyop.client.utility.PathIndex;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.*;

import static monster.psyop.client.Psyop.MC;

public class Config {
    private static final ExclusionStrategy excludeOptional =
            new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaredType() == Optional.class;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            };
    public static final Gson GSON =
            new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.STATIC)
                    .setExclusionStrategies(excludeOptional)
                    .registerTypeAdapter(ImInt.class, new ImIntAdapter())
                    .registerTypeAdapter(ImBoolean.class, new ImBooleanAdapter())
                    .registerTypeAdapter(ImString.class, new ImStringAdapter())
                    .registerTypeAdapter(Item.class, new ItemAdapter())
                    .registerTypeAdapter(Block.class, new BlockAdapter())
                    .setPrettyPrinting()
                    .create();
    private static Config INSTANCE;
    public Inventory inventory = new Inventory();
    public Placing placing = new Placing();
    public Map<String, PersistentGuiSettings> hudElements = new HashMap<>();
    public GuiSettings guiSettings = new GuiSettings();
    public PersistentGuiSettings moduleConfigGui = new PersistentGuiSettings();
    public ModuleConfigSettings moduleConfigSettings = new ModuleConfigSettings();
    public LogsSettings logsSettings = new LogsSettings();
    public PersistentGuiSettings modulesGui = new PersistentGuiSettings();
    public PersistentGuiSettings hudGui = new PersistentGuiSettings();
    public PersistentGuiSettings configGui = new PersistentGuiSettings();
    public PersistentGuiSettings logsGui = new PersistentGuiSettings();
    public PersistentGuiSettings trollingGui = new PersistentGuiSettings();
    public PersistentGuiSettings booksGui = new PersistentGuiSettings();
    public PersistentGuiSettings friendsManagerGui = new PersistentGuiSettings();
    public BookSettings bookSettings = new BookSettings();
    public CoreConfig coreSettings = new CoreConfig();
    public Map<String, ModuleConfig> modules = new HashMap<>();
    public ObjectColor color = new ObjectColor("uwu", new float[]{0.4f, 0.3f, 0.2f, 1.0f});

    // Roles
    public List<RoleType> definedRoles = new ArrayList<>();
    public Map<String, String> userRoles = new HashMap<>();

    public static Config get() {
        return INSTANCE;
    }

    public void hide(Setting<?, ?> setting, boolean state) {
        guiSettings.hiddenLists.put(setting.name, state);
    }

    public boolean isHidden(Setting<?, ?> setting) {
        guiSettings.hiddenLists.putIfAbsent(setting.name, false);
        return guiSettings.hiddenLists.get(setting.name);
    }

    public void save() {
        for (Category category : Categories.INDEX) {
            for (Module module : Psyop.MODULES.getModules(category)) {
                ModuleConfig moduleConfig = new ModuleConfig();
                moduleConfig.active = module.active;
                for (GroupedSettings sg : module.getGroupedSettings()) {
                    SettingGroupConfig groupConfig = new SettingGroupConfig();
                    for (Iterator<Setting<?, ?>> it = sg.get(); it.hasNext(); ) {
                        Setting<?, ?> setting = it.next();

                        if (setting.settingConfig == null || setting.value() == null) {
                            Psyop.debug("{} in {} had a null setting config", setting.name, module.name);
                            continue;
                        }

                        try {
                            setting.settingConfig.fromSetting(setting);
                            Psyop.debug("Setting {} to {} in module {}", setting.name, setting.value().toString(), module.name);
                        } catch (Exception e) {
                            Psyop.debug(e.getLocalizedMessage(), e);
                        }

                        groupConfig.settings.put(setting.name, setting.settingConfig);
                    }
                    moduleConfig.groups.put(sg.name, groupConfig);
                }
                get().modules.put(module.name, moduleConfig);
            }
        }

        definedRoles.clear();
        definedRoles.addAll(FriendManager.roleTypes);

        userRoles.clear();
        for (var entry : FriendManager.roles.entrySet()) {
            userRoles.put(entry.getKey(), entry.getValue().roleType.name);
        }

        hudElements.clear();

        for (HudElement element : HudHandler.getElements()) {
            hudElements.put(element.name(), element.settings);
        }

        ConfigView.populateConfig();

        try {
            String toJson = GSON.toJson(get());

            FileSystem.write(PathIndex.CONFIG, toJson);
        } catch (Exception e) {
            Psyop.error("Config failed to load: {}", e.getLocalizedMessage());
            Psyop.LOG.info("Config failed to load.", e);
        }
    }

    @SneakyThrows
    public void load() {
        if (Files.exists(PathIndex.CONFIG) && Files.isRegularFile(PathIndex.CONFIG)) {
            String file = Files.readString(PathIndex.CONFIG);
            try {
                Config.INSTANCE = GSON.fromJson(file, Config.class);
            } catch (Exception e) {
                FileSystem.write(PathIndex.CLIENT.resolve(file.hashCode() + "_c.json"), file);
                Psyop.LOG.info("Config Failed to load.", e);
                Psyop.error("Config failed to load, reset.", e);
                Config.INSTANCE = this;
            }
        } else {
            Config.INSTANCE = this;
        }
    }

    public void populateModule(Module module) {
        if (modules.containsKey(module.name)) {
            Psyop.debug("Loading config for {}", module.name);
            ModuleConfig moduleConfig = get().modules.get(module.name);
            module.active(moduleConfig.active.get());
            for (GroupedSettings sg : module.getGroupedSettings()) {
                if (!moduleConfig.groups.containsKey(sg.name)) {
                    Psyop.debug("Skipping {} in {]", sg.name, module.name);
                    continue;
                }

                for (Iterator<Setting<?, ?>> it = sg.get(); it.hasNext(); ) {
                    Setting<?, ?> setting = it.next();

                    if (!moduleConfig.groups.get(sg.name).settings.containsKey(setting.name)) {
                        Psyop.debug("Skipping {} - {} in {}", sg.name, setting.name, module.name);
                        continue;
                    }

                    moduleConfig.groups.get(sg.name).settings.get(setting.name).cloneTo(setting.settingConfig);

                    setting.settingConfig.populateSetting(setting);

                    Psyop.debug("Setting {} in {} to {}", setting.name, module.name, setting.value().toString());

                    setting.onPopulated();
                }
            }
        } else {
            Psyop.log("Module {} does not exist?", module.name);
        }
    }

    public static boolean chatFeedback() {
        if (MC.player == null || MC.level == null) {
            return false;
        }

        ViewHandler.get(ConfigView.class);
        return ConfigView.chatFeedback.get();
    }
}
