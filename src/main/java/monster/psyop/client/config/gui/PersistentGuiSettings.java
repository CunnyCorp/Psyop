package monster.psyop.client.config.gui;

import imgui.type.ImBoolean;

public class PersistentGuiSettings {
    public ImBoolean isLoaded = new ImBoolean(false);
    public boolean hasLoaded = false;
    public float x = 0;
    public float y = 0;
    public float width = 64;
    public float height = 64;
}
