package monster.psyop.client.impl.modules.world;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.impl.events.game.OnTick;

public class FastBreak extends Module {
    public FloatSetting breakMultiplier = new FloatSetting.Builder()
            .name("break-multiplier")
            .defaultTo(2.5f)
            .range(0.1f, 10f)
            .addTo(coreGroup);
    public FloatSetting breakAddition = new FloatSetting.Builder()
            .name("break-addition")
            .defaultTo(0.0f)
            .range(0.0f, 10f)
            .addTo(coreGroup);


    public FastBreak() {
        super(Categories.WORLD, "fast-break", "How fast to break blocks.");
    }

    @EventListener
    public void onTick(OnTick.Post event) {
        MC.gameMode.destroyProgress += breakAddition.get();
    }
}
