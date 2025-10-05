package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class LagbackDetector extends Module {
    public BoolSetting waitForGround = new BoolSetting.Builder()
            .name("wait-for-ground")
            .description("Doesn't start ticking down till on ground.")
            .defaultTo(true)
            .addTo(coreGroup);
    public IntSetting lagBackPause = new IntSetting.Builder()
            .name("pause")
            .description("Pauses movement for this many ticks.")
            .defaultTo(9)
            .range(1, 90)
            .addTo(coreGroup);
    public IntSetting spoof = new IntSetting.Builder()
            .name("spoof")
            .description("How many times to spoof per tick.")
            .defaultTo(5)
            .range(1, 90)
            .addTo(coreGroup);


    private int ticks = 0;
    private boolean forwardPressed = false;
    private boolean backwardPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;

    public LagbackDetector() {
        super(Categories.MOVEMENT, "lagback-detector", "Detects lagbacks.");
    }

    @EventListener
    public void onPacket(OnPacket.Received event) {
        if (event.packet() instanceof ClientboundPlayerPositionPacket) {
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
        if (!waitForGround.get() || MC.player.onGround()) {
            ticks--;
        }

        if (ticks >= 0) {
            AutoWalk.resetKeys();

            for (int i = 0; i < spoof.get(); i++) {
                MC.player.tick();
            }
        }

        if (ticks == -1) {
            MC.gui.setOverlayMessage(Component.literal("Movement restored.").withStyle(TextUtils.MODULE_INFO_STYLE), true);
            MC.options.keyUp.setDown(forwardPressed);
            MC.options.keyDown.setDown(backwardPressed);
            MC.options.keyLeft.setDown(leftPressed);
            MC.options.keyRight.setDown(rightPressed);
            MC.options.keyJump.setDown(jumpPressed);
        }
    }
}
