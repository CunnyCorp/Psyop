package monster.psyop.client.config.gui;

import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;

public class CoreConfig {
    public ImInt guiBind = new ImInt(57);
    public ImBoolean moduleCatSnap = new ImBoolean(false);
    public ImBoolean chatFeedback = new ImBoolean(false);
    public ImString textCaseStyle = new ImString("original");
    public ImBoolean useSnakeCase = new ImBoolean(false);
    public ImBoolean useCamelCase = new ImBoolean(false);
    public ImBoolean useKebabCase = new ImBoolean(false);
    public ImBoolean usePascalCase = new ImBoolean(false);
}
