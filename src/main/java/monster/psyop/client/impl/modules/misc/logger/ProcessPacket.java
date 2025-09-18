package monster.psyop.client.impl.modules.misc.logger;

import net.minecraft.network.protocol.Packet;

@FunctionalInterface
public interface ProcessPacket {
    void run(Packet<?> packet);
}
