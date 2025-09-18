package monster.psyop.client.config.gui.books;

import imgui.type.ImInt;

public class PageSettings extends BookSubSettings {
    public final ImInt count = new ImInt(50);

    @Override
    public String getDefaultText() {
        return "Uooohhh !!!! SO CUTE !!! SO FUNNY !!!";
    }

    @Override
    public int getDefaultLength() {
        return 1024;
    }
}
