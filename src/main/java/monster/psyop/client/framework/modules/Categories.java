package monster.psyop.client.framework.modules;

import java.util.ArrayList;

public class Categories {
    public static final ArrayList<Category> INDEX = new ArrayList<>();

    public static final Category
            COMBAT = addCategory("Combat", "Modules related to PvP."),
            PLAYER = addCategory("Player", "Modules related to Player."),
            MOVEMENT = addCategory("Movement", "Modules related to Movement."),
            WORLD = addCategory("World", "Modules that affect the world."),
            RENDER = addCategory("Render", "Modules that add or change rendering."),
            HUD = addCategory("HUD", "Displays HUD elements."),
            CHAT = addCategory("Chat", "Modules that add to/tweak chat."),
            EXPLOITS = addCategory("Exploits", "Exploits and Co"),
            MISC = addCategory("Misc", "Miscellaneous modules that don't fit elsewhere."),
            SILLY = addCategory("Silly", "You probably shouldn't enable this."),
            CLIENT = addCategory("Client", "Internal Client Configuration.");

    public static Category addCategory(String name, String description) {
        Category cat = new Category(name, description);

        INDEX.add(cat);

        return cat;
    }
}
