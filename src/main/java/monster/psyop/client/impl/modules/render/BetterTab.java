package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;

public class BetterTab extends Module {
    public FloatSetting expScaling = new FloatSetting.Builder()
            .name("exp-scaling")
            .defaultTo(0.05f)
            .range(0.0f, 1.0f)
            .addTo(coreGroup);

    public BetterTab() {
        super(Categories.RENDER, "name-tags", "Modifies name-tag rendering");
    }


}
