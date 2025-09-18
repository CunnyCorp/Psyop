package monster.psyop.client.config.gui.books;

import monster.psyop.client.config.gui.RandomizationSettings;
import monster.psyop.client.framework.gui.views.features.BookEditorView;

public class BookSubSettings extends RandomizationSettings {
    public BookEditorView.BookModifier modifier = BookEditorView.BookModifier.NONE;

    @Override
    public String getDefaultText() {
        return "Cunny Client!";
    }

    @Override
    public int getDefaultLength() {
        return 16;
    }
}
