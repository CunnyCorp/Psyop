package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;

public class PlayerTimer extends Module {
    public IntSetting multiplier = new IntSetting.Builder()
            .name("multiplier")
            .description("How many extra ticks to run.")
            .defaultTo(3)
            .addTo(coreGroup);
    public IntSetting burstMultiplier = new IntSetting.Builder()
            .name("burst-multiplier")
            .description("How many times to burst.")
            .defaultTo(5)
            .addTo(coreGroup);
    public IntSetting burstDelay = new IntSetting.Builder()
            .name("burst-delay")
            .description("How long between bursts.")
            .defaultTo(10)
            .addTo(coreGroup);

    public int lastBurst = 0;

    public PlayerTimer() {
        super(Categories.MOVEMENT, "player-timer", "Sends multiple player ticks.");
    }

    @EventListener
    public void onTick(OnTick.Post event) {
        if (lastBurst > 1) {
            lastBurst--;
        }
    }
}
