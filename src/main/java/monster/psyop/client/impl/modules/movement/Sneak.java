package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

public class Sneak extends Module {
    public final BoolSetting silent =
            new BoolSetting.Builder()
                    .name("silent")
                    .description("Only sneak on the server.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final BoolSetting cycle =
            new BoolSetting.Builder()
                    .name("cycle")
                    .description("Sneak every other packet.")
                    .defaultTo(false)
                    .addTo(coreGroup);

    protected boolean shouldSneak = true;

    public Sneak() {
        super(Categories.MOVEMENT, "sneak", "Automatically sneaks.");
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundPlayerInputPacket packet) {
            event.packet(new ServerboundPlayerInputPacket(new Input(packet.input().forward(), packet.input().backward(), packet.input().left(), packet.input().right(), packet.input().jump(), shouldSneak, packet.input().sprint())));
        }
    }

    @Override
    public void update() {
        if (MC.player == null) return;

        if (cycle.get()) shouldSneak = !shouldSneak;
        else shouldSneak = true;

        MC.player.setShiftKeyDown(!silent.value().get() && shouldSneak);
    }
}
