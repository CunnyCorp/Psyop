package monster.psyop.client.config.gui;

import imgui.type.ImBoolean;
import imgui.type.ImString;

public class LogsSettings {
    public ImBoolean commands = new ImBoolean(false);
    public ImBoolean chat = new ImBoolean(false);
    public ImString input = new ImString("");
}
