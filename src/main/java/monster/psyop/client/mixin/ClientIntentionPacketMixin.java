package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.exploits.HostOverride;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientIntentionPacket.class)
public class ClientIntentionPacketMixin {

    @Mutable
    @Shadow
    private String hostName;

    @Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/protocol/handshake/ClientIntent;)V", at = @At("RETURN"))
    private void onInit(int protocolVersion, String hostName, int port, ClientIntent intention, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(HostOverride.class)) {
            HostOverride hostOverrideModule = Psyop.MODULES.get(HostOverride.class);
            if (intention == ClientIntent.LOGIN || (intention == ClientIntent.STATUS && hostOverrideModule.overrideOnPing.get())) {
                this.hostName = hostOverrideModule.newHostname.value().get();
            }
        }
    }
}
