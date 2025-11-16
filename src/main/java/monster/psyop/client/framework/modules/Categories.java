package monster.psyop.client.framework.modules;

public class Categories {
    public static final Category[] INDEX = new Category[12];

    public static final Category
            COMBAT = INDEX[0] = new Category("Combat", "Modules related to PvP."),
            PLAYER = INDEX[2] = new Category("Player", "Modules related to Player."),
            MOVEMENT = INDEX[3] = new Category("Movement", "Modules related to Movement."),
            WORLD = INDEX[4] = new Category("World", "Modules that affect the world."),
            MISC = INDEX[5] = new Category("Misc", "Miscellaneous modules that don't fit elsewhere."),
            RENDER = INDEX[6] = new Category("Render", "Modules that add or change rendering."),
            HUD = INDEX[7] = new Category("HUD", "Displays HUD elements."),
            CHAT = INDEX[8] = new Category("Chat", "Modules that add to/tweak chat."),
            EXPLOITS = INDEX[9] = new Category("Exploits", "Exploits and Co"),
            SILLY = INDEX[10] = new Category("Silly", "You probably shouldn't enable this."),
            CLIENT = INDEX[11] = new Category("Client", "Internal Client Configuration.");
}
