package monster.psyop.client.utility.modules.internal;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TextState {
    public List<TextPart> parts = new ArrayList<>();

    public void addPart(String text) {
        parts.add(new TextPart(text));
    }

    public TextState addPart(String text, Color color) {
        parts.add(new TextPart(text, color));
        return this;
    }
}
