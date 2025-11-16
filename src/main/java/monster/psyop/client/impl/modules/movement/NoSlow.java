package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;

public class NoSlow extends Module {
    public BoolSetting sneaking = new BoolSetting.Builder()
            .name("sneaking")
            .defaultTo(true)
            .addTo(coreGroup);
    public FloatSetting usingItemScale = new FloatSetting.Builder()
            .name("using-item-scale")
            .defaultTo(0.2f)
            .range(0.0f, 1.0f)
            .addTo(coreGroup);

    public NoSlow() {
        super(Categories.MOVEMENT, "no-slow", "Allows you to move faster!");
    }
}
