package monster.psyop.client.framework.friends;

import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;

public class RoleType {
    public String name;
    public String description;
    public ImColorW color;

    public RoleFeatures features;

    public RoleType(String name, String description, ImColorW color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }
}
