package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

import static monster.psyop.client.Psyop.MC;

public class Warp extends Module {
    public IntSetting multiplier = new IntSetting.Builder()
            .name("multiplier")
            .description("How many extra ticks to run.")
            .defaultTo(80)
            .range(1, 100)
            .addTo(coreGroup);
    public BoolSetting onlyAir = new BoolSetting.Builder()
            .name("only-air")
            .description("Only ticks if off the ground.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting goingUp = new BoolSetting.Builder()
            .name("going-up")
            .defaultTo(true)
            .addTo(coreGroup);
    public IntSetting upMultiplier = new IntSetting.Builder()
            .name("up-multiplier")
            .description("How many extra ticks to run.")
            .defaultTo(8)
            .range(1, 20)
            .addTo(coreGroup);
    public FloatSetting maxYUp = new FloatSetting.Builder()
            .name("max-y-up")
            .defaultTo(0.16f)
            .range(0.0f, 2.0f)
            .addTo(coreGroup);
    public BoolSetting goingDown = new BoolSetting.Builder()
            .name("going-down")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting sprint = new BoolSetting.Builder()
            .name("sprint")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting requireSprint = new BoolSetting.Builder()
            .name("require-sprint")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting lagBack = new BoolSetting.Builder()
            .name("lag-back")
            .description("Detects lag backs and pauses movement.")
            .defaultTo(false)
            .addTo(coreGroup);
    public BoolSetting waitForGround = new BoolSetting.Builder()
            .name("wait-for-ground")
            .description("Doesn't start ticking down till on ground.")
            .defaultTo(true)
            .addTo(coreGroup);
    public FloatSetting multiplierDiagonal = new FloatSetting.Builder()
            .name("multiplier-diagonal")
            .defaultTo(1.0f)
            .range(0.0f, 2.0f)
            .addTo(coreGroup);
    public FloatSetting multiplierVertical = new FloatSetting.Builder()
            .name("multiplier-vertical")
            .defaultTo(1.0f)
            .range(0.0f, 2.0f)
            .addTo(coreGroup);
    public IntSetting lagBackPause = new IntSetting.Builder()
            .name("pause")
            .description("Pauses movement for this many ticks.")
            .defaultTo(9)
            .range(1, 90)
            .addTo(coreGroup);

    private int ticks = 0;
    private boolean forwardPressed = false;
    private boolean jumpPressed = false;

    public Warp() {
        super(Categories.MOVEMENT, "warp", "Lets you warp by pressing the jump key.");
    }

    @EventListener
    public void onPacket(OnPacket.Received event) {
        if (lagBack.get() && event.packet() instanceof ClientboundPlayerPositionPacket) {
            MC.gui.setOverlayMessage(Component.literal("Lagged back by AC.").withStyle(TextUtils.MODULE_INFO_STYLE), true);
            if (ticks < 0) {
                forwardPressed = MC.options.keyUp.isDown();
                jumpPressed = MC.options.keyJump.isDown();
            }
            ticks = lagBackPause.get();
        }
    }

    @Override
    public void update() {
        if (sprint.get()) {
            Psyop.MODULES.get(Sprint.class).update();
        }

        if (MC.options.keyUp.isDown()) MC.options.keyJump.setDown(true);

        if (!waitForGround.get() || MC.player.onGround()) {
            ticks--;
        }

        if (ticks >= 0) {
            AutoWalk.resetKeys();
        }

        if (ticks == -1) {
            MC.gui.setOverlayMessage(Component.literal("Movement restored.").withStyle(TextUtils.MODULE_INFO_STYLE), true);
            MC.options.keyUp.setDown(forwardPressed);
            MC.options.keyJump.setDown(jumpPressed);
        }
    }
}
