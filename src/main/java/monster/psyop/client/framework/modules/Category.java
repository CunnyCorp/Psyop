package monster.psyop.client.framework.modules;

import lombok.Getter;
import lombok.Setter;
import monster.psyop.client.utility.StringUtils;

public class Category {
    public final String name;
    public final String description;
    @Getter
    @Setter
    private String label;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.label = StringUtils.readable(name);
    }

    public java.awt.Color color() {
        switch (this.name) {
            case "Combat":
                return new java.awt.Color(220, 80, 60); // Reddish
            case "Movement":
                return new java.awt.Color(60, 180, 220); // Cyan/Blue
            case "World":
                return new java.awt.Color(100, 200, 100); // Green
            case "Misc":
                return new java.awt.Color(180, 180, 180); // Gray
            case "Render":
                return new java.awt.Color(200, 120, 220); // Purple
            case "Chat":
                return new java.awt.Color(220, 180, 60); // Yellow/Orange
            case "Exploits":
                return new java.awt.Color(220, 60, 180); // Magenta/Pink
            default:
                return new java.awt.Color(150, 150, 220); // Default blue
        }
    }
}
