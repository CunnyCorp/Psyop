package monster.psyop.client.framework.gui.views;

import imgui.type.ImBoolean;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.utility.StringUtils;

public abstract class View {
    public PersistentGuiSettings settings = new PersistentGuiSettings();
    private String readableName;

    public abstract String name();

    public String displayName() {
        if (readableName == null) {
            readableName = StringUtils.readable(name());
        }

        String name = readableName;

        if (Config.get().guiSettings.consistentViews.contains(name())) {
            name += " (C)";
        }

        return name;
    }

    public abstract void show();

    public abstract void populateSettings(Config config);

    public ImBoolean state() {
        return ViewHandler.state(getClass());
    }

    public void load() {
        ViewHandler.add(this);
    }
}
