package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;

public class NoRender extends Module {
    public final BoolSetting beams =
            new BoolSetting.Builder()
                    .name("beams")
                    .description("Prevents beam rendering.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final BoolSetting beamsDepth =
            new BoolSetting.Builder()
                    .name("beams-depth")
                    .description("Prevents beams from applying colors.")
                    .defaultTo(false)
                    .addTo(coreGroup);


    public NoRender() {
        super(Categories.RENDER, "no-render", "Disables rendering of certain things.");
    }


}
