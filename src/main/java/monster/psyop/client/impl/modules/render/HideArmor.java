package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;

public class HideArmor extends Module {
    public final BoolSetting trims =
            new BoolSetting.Builder()
                    .name("trims")
                    .description("Hides trims.")
                    .defaultTo(true)
                    .addTo(coreGroup);

    public HideArmor() {
        super(Categories.RENDER, "hide-armor", "Lets you hide armor.");
    }
}
