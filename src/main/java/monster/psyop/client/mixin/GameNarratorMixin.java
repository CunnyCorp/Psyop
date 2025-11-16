package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.misc.AntiNarrator;
import net.minecraft.client.GameNarrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameNarrator.class)
public class GameNarratorMixin {
    @Inject(method = "narrateMessage", at = @At("HEAD"), cancellable = true)
    public void narrateMessage(String string, boolean bl, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(AntiNarrator.class)) ci.cancel();
    }
}
