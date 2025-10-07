package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;

public class AutoWalk extends Module {
    public final BoolSetting autoJump = new BoolSetting.Builder()
            .name("auto-jump")
            .description("Holds jump key.")
            .defaultTo(true)
            .addTo(coreGroup);
    public IntSetting jumpTime = new IntSetting.Builder()
            .name("jump-time")
            .description("Jumps for this many ticks.")
            .defaultTo(20)
            .range(1, 100)
            .addTo(coreGroup);
    public IntSetting jumpWait = new IntSetting.Builder()
            .name("jump-wait")
            .description("How long between holding jump.")
            .defaultTo(10)
            .range(1, 100)
            .addTo(coreGroup);

    private int ticks = 0;
    private int jumpHeldTicks = 0;
    private int lastJumpTicks = 0;

    public AutoWalk() {
        super(Categories.MOVEMENT, "auto-walk", "Automatically presses input keys.", AutoWalk::resetKeys, AutoWalk::resetKeys);
    }


    @Override
    public void update() {
        if (autoJump.get()) {
            if (lastJumpTicks > 0) {
                MC.options.keyJump.setDown(false);
                lastJumpTicks--;
            } else {
                MC.options.keyJump.setDown(jumpHeldTicks >= 0);

                if (jumpHeldTicks >= 0) {
                    jumpHeldTicks--;
                }

                if (jumpHeldTicks < 0) {
                    lastJumpTicks = jumpWait.get();
                    jumpHeldTicks = jumpTime.get();
                }
            }
        }

        MC.options.keyUp.setDown(true);
    }

    public static void resetKeys() {
        MC.options.keyJump.setDown(false);
        MC.options.keyUp.setDown(false);
    }
}
