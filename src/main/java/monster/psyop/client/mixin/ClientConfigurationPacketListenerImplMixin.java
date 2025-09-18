package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.exploits.LoginFuckery;
import monster.psyop.client.utility.StringUtils;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.server.packs.repository.KnownPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public class ClientConfigurationPacketListenerImplMixin {
    @Inject(method = "handleSelectKnownPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"), cancellable = true)
    public void isAcceptingMessages(ClientboundSelectKnownPacks clientboundSelectKnownPacks, CallbackInfo ci) {
        LoginFuckery module = Liberty.MODULES.get(LoginFuckery.class);

        if (!module.active()) {
            return;
        }

        for (int y = 0; y < module.knownAmount.get(); y++) {
            List<KnownPack> packs = new ArrayList<>();

            for (int i = 0; i < module.packAmount.get(); i++) {
                packs.add(new KnownPack(StringUtils.randomText(module.fieldSizes.get(), true), StringUtils.randomText(module.fieldSizes.get(), true), StringUtils.randomText(module.fieldSizes.get(), true)));
            }

            ((ClientCommonPacketListenerImpl) (Object) this).send(new ServerboundSelectKnownPacks(packs));
        }
    }
}
