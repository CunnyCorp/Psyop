package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;

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
            .description("How long to wait between bursts in ticks.")
            .defaultTo(5)
            .range(0, 20)
            .addTo(coreGroup);
    public static int lastBurst = 5;

    public PlayerTimer() {
        super(Categories.MOVEMENT, "player-timer", "Sends multiple player ticks.");
    }
}
