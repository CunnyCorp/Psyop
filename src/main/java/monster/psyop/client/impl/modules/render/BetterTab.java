package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;

public class BetterTab extends Module {
    public BoolSetting customTab = new BoolSetting.Builder()
            .name("custom-tab")
            .description("Toggles the custom player tab overlay.")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting customColor = new ColorSetting.Builder()
            .name("custom-color")
            .description("The color of the player tab overlay background.")
            .defaultTo(new float[]{1.0f, 0.0f, 1.0f,0.12f})
            .addTo(coreGroup);
    public ColorSetting customColor2 = new ColorSetting.Builder()
            .name("custom-color-2")
            .description("The second color of the player tab overlay background.")
            .defaultTo(new float[]{1.0f, 0.0f, 1.0f,0.16f})
            .addTo(coreGroup);

    public BetterTab() {
        super(Categories.RENDER, "better-tab", "makes tab better");
    }
}
