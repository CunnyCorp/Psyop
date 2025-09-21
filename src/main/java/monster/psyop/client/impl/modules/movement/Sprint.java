package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Input;

public class Sprint extends Module {
    public final BoolSetting ifNot =
            new BoolSetting.Builder()
                    .name("if-not")
                    .description("Only set sprinting if not already sprinting.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final BoolSetting noBlindness =
            new BoolSetting.Builder()
                    .name("no-blindness")
                    .description("Only sprint if not in blinded.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final BoolSetting omni =
            new BoolSetting.Builder()
                    .name("omni")
                    .description("Sprint in any direction.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final BoolSetting noSneak =
            new BoolSetting.Builder()
                    .name("no-sneak")
                    .description("Only sprint if not sneaking.")
                    .defaultTo(true)
                    .addTo(coreGroup);


    public Sprint() {
        super(Categories.MOVEMENT, "sprint", "Automatically sprints.");
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundPlayerInputPacket packet) {
            event.packet(new ServerboundPlayerInputPacket(new Input(packet.input().forward(), packet.input().backward(), packet.input().left(), packet.input().right(), packet.input().jump(), packet.input().shift(), true)));
        }
    }

    @Override
    public void update() {
        if (MC.player == null) return;

        // Not enough food.
        if (MC.player.getFoodData().getFoodLevel() <= 6.0f) {
            MC.player.setSprinting(false);
            return;
        }

        if (noBlindness.get() && MC.player.hasEffect(MobEffects.BLINDNESS)) {
            MC.player.setSprinting(false);
            return;
        }

        if (!MC.player.isSprinting() || !ifNot.get()) {
            if (noSneak.get() && MC.player.isShiftKeyDown()) {
                MC.player.setSprinting(false);
                return;
            }

            if (!omni.get() && !MC.player.input.hasForwardImpulse()) {
                MC.player.setSprinting(false);
                return;
            }

            MC.player.setSprinting(true);
        }
    }

    public boolean shouldStopSprinting() {
        // Technically impossible but ?
        if (MC.player == null) {
            return true;
        }

        if (!omni.get()) {
            if (!MC.player.input.hasForwardImpulse()) {
                return true;
            }
        }

        if (noSneak.get() && MC.player.isShiftKeyDown()) {
            return true;
        }

        if (noBlindness.get() && MC.player.hasEffect(MobEffects.BLINDNESS)) {
            return true;
        }

        return MC.player.getFoodData().getFoodLevel() <= 6.0f || MC.player.isPassenger();
    }
}
