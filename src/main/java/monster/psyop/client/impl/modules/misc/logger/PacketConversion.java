package monster.psyop.client.impl.modules.misc.logger;

import monster.psyop.client.Liberty;
import net.minecraft.network.protocol.Packet;

public record PacketConversion(ProcessPacket process) {
    public void safeCall(Packet<?> packet) {
        try {
            process.run(packet);
        } catch (Exception exception) {
            Liberty.LOG.error("Unknown exception while trying to log a packet.", exception);
        }
    }
}
