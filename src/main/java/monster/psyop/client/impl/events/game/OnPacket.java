package monster.psyop.client.impl.events.game;

import monster.psyop.client.framework.events.Event;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

public abstract class OnPacket extends Event {
    protected Packet<? extends PacketListener> packet;
    public boolean wasModified = false;

    public OnPacket() {
        super(false);
    }

    public OnPacket(boolean cancellable) {
        super(cancellable);
    }

    public Packet<? extends PacketListener> packet() {
        return this.packet;
    }

    public void packet(Packet<? extends PacketListener> packet) {
        this.packet = packet;
        this.wasModified = true;
    }

    public static class Received extends OnPacket {
        public static Received INSTANCE = new Received();

        public Received() {
            super(true);
            INSTANCE = this;
        }

        public static Received get(Packet<? extends PacketListener> packet) {
            INSTANCE.refresh();
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }

    public static class Send extends OnPacket {
        public static Send INSTANCE = new Send();

        public Send() {
            super(true);
            INSTANCE = this;
        }

        public static Send get(Packet<? extends PacketListener> packet) {
            INSTANCE.refresh();
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }

    public static class Sent extends OnPacket {
        public static Sent INSTANCE = new Sent();

        public Sent() {
            super();
            INSTANCE = this;
        }

        public static Sent get(Packet<? extends PacketListener> packet) {
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }
}
