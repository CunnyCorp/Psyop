package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.events.game.OnPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;

public class NoSwing extends Module {
    public NoSwing() {
        super(Categories.MISC, "no-swing", "Cancels swing packets.");
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundSwingPacket) {
            event.cancel();
        }
    }
}
