package monster.psyop.client.utility.modules.internal;

import java.awt.*;

public class TextPart {
    public String text;
    public Color color = Color.WHITE;

    public TextPart(String text) {
        this.text = text;
    }

    public TextPart(String text, Color color) {
        this.text = text;
        this.color = color;
    }
}
