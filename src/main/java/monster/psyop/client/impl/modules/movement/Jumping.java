package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.impl.events.game.OnTick;

public class Jumping extends Module {
    public final FloatSetting jumpHeight =
            new FloatSetting.Builder()
                    .name("jump-height")
                    .description("Jump height multiplier.")
                    .defaultTo(1.0f)
                    .range(0.5f, 1.5f)
                    .addTo(coreGroup);
    public final FloatSetting jumpSprintMulti =
            new FloatSetting.Builder()
                    .name("jump-sprint-multi")
                    .description("Jump sprint speed multiplier.")
                    .defaultTo(1.0f)
                    .range(0.5f, 1.5f)
                    .addTo(coreGroup);
    public final BoolSetting removeDelay =
            new BoolSetting.Builder()
                    .name("remove-delay")
                    .description("Removes the jumping delay.")
                    .defaultTo(true)
                    .addTo(coreGroup);

    public Jumping() {
        super(Categories.MOVEMENT, "jumping", "Removes jump delay.");
    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        if (removeDelay.get()) {
            assert MC.player != null;
            MC.player.noJumpDelay = 0;
        }
    }
}
