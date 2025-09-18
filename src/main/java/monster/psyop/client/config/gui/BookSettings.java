package monster.psyop.client.config.gui;

import imgui.type.ImBoolean;
import imgui.type.ImInt;
import monster.psyop.client.config.gui.books.BookSubSettings;
import monster.psyop.client.config.gui.books.PageSettings;

public class BookSettings {
    public BookSubSettings title = new BookSubSettings();
    public PageSettings pages = new PageSettings();
    public ImBoolean autoSign = new ImBoolean(false);
    public ImInt signDelay = new ImInt(30);
}
