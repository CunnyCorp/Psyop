package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;

public class Reach extends Module {
    public final FloatSetting entity =
            new FloatSetting.Builder()
                    .name("entity")
                    .description("Modifies entity reach.")
                    .defaultTo(2f)
                    .range(0.5f, 5.0f)
                    .addTo(coreGroup);
    public final FloatSetting block =
            new FloatSetting.Builder()
                    .name("block")
                    .description("Modifies block reach.")
                    .defaultTo(2f)
                    .range(0.5f, 5.0f)
                    .addTo(coreGroup);


    public Reach() {
        super(Categories.MISC, "reach", "Modify reach.");
    }


}
