package monster.psyop.client.impl.modules.world;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;

public class BreakDelay extends Module {
    public final IntSetting delay =
            new IntSetting.Builder()
                    .name("delay")
                    .description("Delay for breaking blocks.")
                    .defaultTo(0)
                    .range(0, 10)
                    .addTo(coreGroup);

    public BreakDelay() {
        super(Categories.WORLD, "break-delay", "The delay between breaking blocks.");
    }

    @EventListener
    public void onTick(OnTick.Pre ignored) {
        assert MC.gameMode != null;
        MC.gameMode.destroyDelay = delay.get();
    }
}
