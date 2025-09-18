package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.movement.AntiPush;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static monster.psyop.client.Liberty.MC;

@Mixin(FishingHook.class)
public class FishingHookMixin {
    @Inject(method = "pullEntity", at = @At("HEAD"), cancellable = true)
    public void preventHookPull(Entity entity, CallbackInfo ci) {
        if (Liberty.MODULES.isActive(AntiPush.class)) {
            AntiPush module = Liberty.MODULES.get(AntiPush.class);

            if (module.hooking.get()) {
                if (entity.getId() == MC.player.getId()) {
                    ci.cancel();
                }
            }
        }
    }
}
