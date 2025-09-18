package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.events.game.OnBlockModify;
import monster.psyop.client.impl.modules.combat.AntiKb;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    public void handleBlockUpdate(
            ClientboundBlockUpdatePacket clientboundBlockUpdatePacket, CallbackInfo ci) {
        Liberty.EVENT_HANDLER.call(
                OnBlockModify.Update.get(
                        clientboundBlockUpdatePacket.getBlockState(), clientboundBlockUpdatePacket.getPos()));
    }

    @Inject(method = "handleExplosion", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"), cancellable = true)
    public void handleExplosion(
            ClientboundExplodePacket clientboundExplodePacket, CallbackInfo ci) {
        if (Liberty.MODULES.isActive(AntiKb.class)) {
            AntiKb module = Liberty.MODULES.get(AntiKb.class);

            if (module.skipCaring() || !module.explosions.get()) {
                return;
            }

            if (module.grimMode.get()) {
                ci.cancel();
            }
        }
    }
}
