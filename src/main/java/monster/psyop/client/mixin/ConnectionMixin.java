package monster.psyop.client.mixin;

import io.netty.channel.ChannelFutureListener;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.events.game.OnGameQuit;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static monster.psyop.client.Psyop.MC;

@Mixin(value = Connection.class, priority = 99999999)
public abstract class ConnectionMixin {

    @Shadow
    public abstract void send(Packet<?> packet, @Nullable ChannelFutureListener packetSendListener, boolean bl);

    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void handlePacketHead(
            Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (listener instanceof ClientPacketListener) {
            OnPacket.Received event = OnPacket.Received.get(packet);
            Psyop.EVENT_HANDLER.call(event);

            if (event.wasModified) {
                packet.handle((T) listener);
                ci.cancel();
                return;
            }

            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "disconnect*", at = @At("HEAD"), cancellable = true)
    public void disconnect(Component component, CallbackInfo ci) {
        OnGameQuit event = OnGameQuit.get(component);
        Psyop.EVENT_HANDLER.call(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void sendHead(
            Packet<?> packet, ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        OnPacket.Send event = OnPacket.Send.get(packet);

        Psyop.EVENT_HANDLER.call(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        if (MC.player != null && event.wasModified) {
            PacketUtils.send(event.packet());
            Psyop.EVENT_HANDLER.call(OnPacket.Sent.get(event.packet()));

            ci.cancel();
        }
    }

    @Inject(
            method =
                    "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            at = @At("TAIL")
    )
    public void sendTail(
            Packet<?> packet, ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        Psyop.EVENT_HANDLER.call(OnPacket.Sent.get(packet));
    }
}
