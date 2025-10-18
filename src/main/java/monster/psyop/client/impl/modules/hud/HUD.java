package monster.psyop.client.impl.modules.hud;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;

public abstract class HUD extends Module {
    public final BoolSetting move = new BoolSetting.Builder()
            .name("move")
            .description("Middle Click somewhere on the screen to move the HUD element.")
            .defaultTo(false)
            .addTo(coreGroup);
    public final BoolSetting stopMove = new BoolSetting.Builder()
            .name("stop-move")
            .description("Automatically toggle move off.")
            .defaultTo(true)
            .addTo(coreGroup);
    public final IntSetting xPos = new IntSetting.Builder()
            .name("x")
            .defaultTo(100)
            .range(0, 4000)
            .addTo(coreGroup);
    public final IntSetting yPos = new IntSetting.Builder()
            .name("y")
            .defaultTo(100)
            .range(0, 4000)
            .addTo(coreGroup);


    public HUD(String name, String description) {
        super(Categories.HUD, name, description);
    }

    public HUD(Category category, String name, String description) {
        super(category, name, description);
    }

    public void render() {
    }
}
