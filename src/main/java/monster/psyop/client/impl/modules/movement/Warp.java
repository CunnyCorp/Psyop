package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class Warp extends Module {
    public IntSetting multiplier = new IntSetting.Builder()
            .name("multiplier")
            .description("How many extra ticks to run.")
            .defaultTo(9)
            .range(1, 20)
            .addTo(coreGroup);
    public BoolSetting onlyAir = new BoolSetting.Builder()
            .name("only-air")
            .description("Only ticks if off the ground.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting lagBack = new BoolSetting.Builder()
            .name("lag-back")
            .description("Detects lag backs and pauses movement.")
            .defaultTo(true)
            .addTo(coreGroup);
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

    @EventListener(priority = 999)
    public void onTick(OnTick.Pre event) {
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
