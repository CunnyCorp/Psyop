package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;

public class LongJump extends Module {
    public IntSetting multiplier = new IntSetting.Builder()
            .name("multiplier")
            .description("How many extra ticks to run.")
            .defaultTo(3)
            .addTo(coreGroup);
    public static int queuedRuns = 0;


    public LongJump() {
        super(Categories.MOVEMENT, "long-jump", "Allows you to long jump easier.");
    }

    @EventListener
    public void onTickPre(OnTick.Pre event) {
        if (!MC.player.onGround()) {
            queuedRuns = multiplier.get();
        }
    }
}
