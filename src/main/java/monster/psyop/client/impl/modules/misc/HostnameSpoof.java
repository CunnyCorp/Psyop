package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.StringSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;

public class HostnameSpoof extends Module {
    public StringSetting hostname = new StringSetting.Builder()
            .name("hostname")
            .defaultTo("psyop.monster")
            .build();

    public HostnameSpoof() {
        super(Categories.MISC, "hostname-spoof", "Lets you spoof the hostname");
    }

    @EventListener(inGame = false)
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ClientIntentionPacket packet) {
            event.packet(new ClientIntentionPacket(packet.protocolVersion(), hostname.get(), packet.port(), packet.intention()));
        }
    }
}

