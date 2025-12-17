package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;

public class ItemView extends Module {
    public BoolSetting walls = new BoolSetting.Builder()
            .name("walls")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting modifyItemColor = new BoolSetting.Builder()
            .name("modify-item-color")
            .description("Modifies item colors.")
            .defaultTo(true)
            .addTo(coreGroup);
    public ColorSetting itemColor = new ColorSetting.Builder()
            .name("item-color")
            .defaultTo(new float[]{0.298f, 0.902f, 0.871f, 0.6f})
            .visible((v) -> modifyItemColor.get())
            .addTo(coreGroup);

    public ItemView() {
        super(Categories.RENDER, "item-view", "Allows for modifications to item rendering values.",
                () -> {
                    if (MC.level != null) MC.level.clearTintCaches();
                },
                () -> {
                    if (MC.level != null) MC.level.clearTintCaches();
                }
        );
    }

}
