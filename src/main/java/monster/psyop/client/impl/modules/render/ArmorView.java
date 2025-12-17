package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;

public class ArmorView extends Module {
    public BoolSetting hideArmor = new BoolSetting.Builder()
            .name("hide-armor")
            .defaultTo(false)
            .addTo(coreGroup);
    public BoolSetting hideWings = new BoolSetting.Builder()
            .name("hide-wings")
            .defaultTo(false)
            .addTo(coreGroup);
    public BoolSetting hideTrims = new BoolSetting.Builder()
            .name("hide-trims")
            .defaultTo(false)
            .addTo(coreGroup);
    public BoolSetting modifyArmorColor = new BoolSetting.Builder()
            .name("modify-armor-color")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting armorColor = new ColorSetting.Builder()
            .name("armor-color")
            .defaultTo(new float[]{0.298f, 0.902f, 0.871f, 0.6f})
            .visible((v) -> modifyArmorColor.get())
            .addTo(coreGroup);
    public BoolSetting modifyWingsColor = new BoolSetting.Builder()
            .name("modify-wings-color")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting wingsColor = new ColorSetting.Builder()
            .name("wings-color")
            .defaultTo(new float[]{0.298f, 0.902f, 0.871f, 0.6f})
            .visible((v) -> modifyWingsColor.get())
            .addTo(coreGroup);
    public BoolSetting modifyCapeColor = new BoolSetting.Builder()
            .name("modify-cape-color")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting capeColor = new ColorSetting.Builder()
            .name("cape-color")
            .defaultTo(new float[]{0.298f, 0.902f, 0.871f, 0.6f})
            .visible((v) -> modifyCapeColor.get())
            .addTo(coreGroup);
    public BoolSetting modifyTrimsColor = new BoolSetting.Builder()
            .name("modify-trims-color")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting trimsColor = new ColorSetting.Builder()
            .name("trims-color")
            .defaultTo(new float[]{0.298f, 0.902f, 0.871f, 0.6f})
            .visible((v) -> modifyTrimsColor.get())
            .addTo(coreGroup);


    public ArmorView() {
        super(Categories.RENDER, "armor-view", "Modifies armor rendering.");
    }
}
