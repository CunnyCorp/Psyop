package monster.psyop.client.mixin.anti_ban;

import io.netty.channel.ChannelHandlerContext;
import monster.psyop.client.Liberty;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 373017)
public abstract class ConnectionMixin {

    @Inject(method = "exceptionCaught*", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", ordinal = 0), cancellable = true)
    public void disconnectBecauseOfClient(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
        Liberty.warn("Your client made an oopsie: {}", throwable.getLocalizedMessage());
        ci.cancel();
    }

    @Inject(method = "exceptionCaught*", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", ordinal = 1), cancellable = true)
    public void disconnectBecauseOfServer(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
        Liberty.warn("The server made an oopsie: {}", throwable.getLocalizedMessage());
        ci.cancel();
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/chat/Component;)V"), cancellable = true)
    public void disconnectFromBadPacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        Liberty.warn("The server made an oopsie, ignored.");
        ci.cancel();
    }
}
