package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;

public class BetterTab extends Module {
    public BoolSetting customTab = new BoolSetting.Builder()
            .name("custom-tab")
            .description("Toggles the custom player tab overlay.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting ping = new BoolSetting.Builder()
            .name("ping")
            .description("Allows you to see the ping of players in the player tab overlay.")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting pingColor = new ColorSetting.Builder()
            .name("ping-color")
            .description("The color for ping numbers.")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 1.0f})
            .addTo(coreGroup);
    public IntSetting xOffset = new IntSetting.Builder()
            .name("x-offset")
            .description("what to offset the ping by.")
            .defaultTo(-20)
            .range(-120, 120)
            .addTo(coreGroup);
    public IntSetting yOffset = new IntSetting.Builder()
            .name("y-offset")
            .description("what to offset the ping by.")
            .defaultTo(2)
            .range(-120, 120)
            .addTo(coreGroup);
    public ColorSetting defaultColor = new ColorSetting.Builder()
            .name("default-color")
            .description("The color for everyone else.")
            .defaultTo(new float[]{0.906f, 0.639f, 0.906f, 1.0f})
            .addTo(coreGroup);
    public ColorSetting friendColor = new ColorSetting.Builder()
            .name("friend-color")
            .description("The color for friends.")
            .defaultTo(new float[]{0.498f, 0.949f, 0.949f, 1.0f})
            .addTo(coreGroup);
    public ColorSetting selfColor = new ColorSetting.Builder()
            .name("self-color")
            .description("The color for yourself.")
            .defaultTo(new float[]{1.0f, 0.231f, 1.0f, 1.0f})
            .addTo(coreGroup);
    public ColorSetting customColor = new ColorSetting.Builder()
            .name("custom-color")
            .description("The color of the player tab overlay background.")
            .defaultTo(new float[]{1.0f, 0.0f, 1.0f, 0.12f})
            .addTo(coreGroup);
    public ColorSetting customColor2 = new ColorSetting.Builder()
            .name("custom-color-2")
            .description("The second color of the player tab overlay background.")
            .defaultTo(new float[]{1.0f, 0.0f, 1.0f, 0.16f})
            .addTo(coreGroup);

    public BetterTab() {
        super(Categories.RENDER, "better-tab", "makes tab better");
    }
}
