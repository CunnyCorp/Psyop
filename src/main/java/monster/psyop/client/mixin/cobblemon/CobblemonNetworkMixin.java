package monster.psyop.client.mixin.cobblemon;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.api.net.NetworkPacket;
import monster.psyop.client.Liberty;
import monster.psyop.client.impl.events.cobblemon.CMPacketEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static monster.psyop.client.Liberty.MC;

@Mixin(value = CobblemonNetwork.class, remap = false)
public abstract class CobblemonNetworkMixin {
    @Inject(method = "sendToServer", at = @At(value = "HEAD"), cancellable = true)
    public void onSendToServer(NetworkPacket<?> packet, CallbackInfo ci) {
        CMPacketEvent.Send event = CMPacketEvent.Send.get(packet);

        Liberty.EVENT_HANDLER.call(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        if (MC.player != null) {
            Cobblemon.implementation.getNetworkManager().sendToServer(event.packet);
            ci.cancel();
        }

        Liberty.EVENT_HANDLER.call(CMPacketEvent.Sent.get(event.packet));
    }
}
