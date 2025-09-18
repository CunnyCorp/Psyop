package monster.psyop.client.config.gui;

import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;

public class RandomizationSettings {
    public final ImString text = new ImString(getDefaultText());
    public final ImInt length = new ImInt(getDefaultLength());
    public final ImBoolean unicode = new ImBoolean(false);

    public String getDefaultText() {
        return "Default Text";
    }

    public int getDefaultLength() {
        return 128;
    }
}
