package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;

public class Phase extends Module {
    public final BoolSetting ignoreMinor = new BoolSetting.Builder()
            .name("ignore-minor")
            .description("Ignore minor horizontal collisions from allowing you to phase.")
            .defaultTo(true)
            .addTo(coreGroup);
    public final FloatSetting minDistance = new FloatSetting.Builder()
            .name("min-distance")
            .description("Minimum distance before cancelling push-out.")
            .defaultTo(0.0f)
            .range(0.0f, 1.0f)
            .addTo(coreGroup);


    public Phase() {
        super(Categories.MOVEMENT, "phase", "Allows you to move into walls once you're partially inside.");
    }
}
