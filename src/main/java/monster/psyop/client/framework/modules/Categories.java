package monster.psyop.client.framework.modules;

public class Categories {
    public static final Category[] INDEX = new Category[7];

    public static final Category
            COMBAT = INDEX[0] = new Category("Combat", "Modules related to PvP."),
            MOVEMENT = INDEX[1] = new Category("Movement", "Modules related to Movement."),
            WORLD = INDEX[2] = new Category("World", "Modules that affect the world."),
            MISC = INDEX[3] = new Category("Misc", "Miscellaneous modules that don't fit elsewhere."),
            RENDER = INDEX[4] = new Category("Render", "Modules that add or change rendering."),
            CHAT = INDEX[5] = new Category("Chat", "Modules that add to/tweak chat."),
            EXPLOITS = INDEX[6] = new Category("Exploits", "Exploits and Co");
}
