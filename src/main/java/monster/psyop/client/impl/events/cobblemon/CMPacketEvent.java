package monster.psyop.client.impl.events.cobblemon;

import com.cobblemon.mod.common.api.net.NetworkPacket;
import monster.psyop.client.framework.events.Event;

public abstract class CMPacketEvent extends Event {
    public NetworkPacket<?> packet;

    public CMPacketEvent() {
        super(false);
    }

    public CMPacketEvent(boolean cancellable) {
        super(cancellable);
    }

    public static class Received extends CMPacketEvent {
        public static Received INSTANCE = new Received();

        public Received() {
            super(true);
            INSTANCE = this;
        }

        public static Received get(NetworkPacket<?> packet) {
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }

    public static class Send extends CMPacketEvent {
        public static Send INSTANCE = new Send();

        public Send() {
            super(true);
            INSTANCE = this;
        }

        public static Send get(NetworkPacket<?> packet) {
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }

    public static class Sent extends CMPacketEvent {
        public static Sent INSTANCE = new Sent();

        public Sent() {
            super();
            INSTANCE = this;
        }

        public static Sent get(NetworkPacket<?> packet) {
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }
}
