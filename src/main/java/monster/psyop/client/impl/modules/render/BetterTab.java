package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;

public class BetterTab extends Module {
    public BoolSetting customTab = new BoolSetting.Builder()
            .name("custom-tab")
            .description("Toggles the custom player tab overlay.")
            .defaultTo(true)
            .addTo(coreGroup);

    public BetterTab() {
        super(Categories.RENDER, "better-tab", "makes tab better");
    }
}
