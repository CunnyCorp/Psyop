package monster.psyop.client.config.modules;

import imgui.type.ImBoolean;

import java.util.HashMap;
import java.util.Map;

public class ModuleConfig {
    public ImBoolean active = new ImBoolean(false);
    public Map<String, SettingGroupConfig> groups = new HashMap<>();
}
