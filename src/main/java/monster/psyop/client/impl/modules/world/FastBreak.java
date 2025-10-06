package monster.psyop.client.impl.modules.world;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

public class FastBreak extends Module {
    public FloatSetting breakMultiplier =new FloatSetting.Builder()
            .name("break-multiplier")
            .defaultTo(2.5f)
            .range(0.1f, 10f)
            .addTo(coreGroup);
    public FloatSetting breakAddition = new FloatSetting.Builder()
            .name("break-addition")
            .defaultTo(0.0f)
            .range(0.0f, 10f)
            .addTo(coreGroup);
    public BoolSetting instant = new BoolSetting.Builder()
            .name("instant")
            .defaultTo(false)
            .addTo(coreGroup);


    public FastBreak() {
        super(Categories.WORLD, "fast-break", "How fast to break blocks.");
    }

    @EventListener
    public void onTick(OnTick.Post event) {
        MC.gameMode.destroyProgress += breakAddition.get();
    }

    @EventListener
    public void onPacket(OnPacket.Sent event) {
        if (instant.get() && event.packet() instanceof ServerboundPlayerActionPacket packet) {
            if (packet.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().relative(Direction.UP, 1337), packet.getDirection()));
                PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, packet.getPos(), packet.getDirection()));
            }
        }
    }
}
