package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
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
            .description("How long to wait between bursts in ticks.")
            .defaultTo(5)
            .range(0, 20)
            .addTo(coreGroup);
    public BoolSetting whileJumping = new BoolSetting.Builder()
            .name("while-jumping")
            .description("Only activates if you are in the air and holding the jump button.")
            .defaultTo(false)
            .addTo(coreGroup);

    public static int queuedRuns = 0;
    public static int lastBurst = 5;

    public PlayerTimer() {
        super(Categories.MOVEMENT, "player-timer", "Sends multiple player ticks.");
    }
}
