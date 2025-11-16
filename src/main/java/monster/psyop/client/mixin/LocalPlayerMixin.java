package monster.psyop.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.events.game.OnScreen;
import monster.psyop.client.impl.modules.movement.NoSlow;
import monster.psyop.client.impl.modules.movement.Phase;
import monster.psyop.client.impl.modules.movement.Sprint;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static monster.psyop.client.Psyop.MC;

@Mixin(value = LocalPlayer.class, priority = 777)
public class LocalPlayerMixin {
    @Inject(
            method = "openTextEdit",
            at = @At("HEAD"),
            cancellable = true)
    public void setScreen(SignBlockEntity signBlockEntity, boolean bl, CallbackInfo ci) {
        OnScreen.SignEdit event = OnScreen.SignEdit.INSTANCE;
        Psyop.EVENT_HANDLER.call(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "moveTowardsClosestSpace",
            at = @At("HEAD"),
            cancellable = true)
    public void moveTowardsClosestSpace(double d, double e, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(Phase.class)) {
            double dist = Math.abs((Math.abs(d) + Math.abs(e)) - (Math.abs(MC.player.getX()) + MC.player.getZ()));

            Phase module = Psyop.MODULES.get(Phase.class);


            if (module.ignoreMinor.get() && MC.player.minorHorizontalCollision) {
                return;
            }

            if (dist >= Psyop.MODULES.get(Phase.class).minDistance.get()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "shouldStopRunSprinting", at = @At("HEAD"), cancellable = true)
    public void shouldStopRunSprinting(CallbackInfoReturnable<Boolean> cir) {
        if (Psyop.MODULES.isActive(Sprint.class)) {
            Sprint module = Psyop.MODULES.get(Sprint.class);

            cir.setReturnValue(module.shouldStopSprinting());
        }
    }

    @Redirect(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal = 1))
    public Vec2 modifyInput(Vec2 instance, float f) {
        if (Psyop.MODULES.isActive(NoSlow.class)) {
            NoSlow module = Psyop.MODULES.get(NoSlow.class);

            return instance.scale(module.usingItemScale.get());
        }
        return instance.scale(f);
    }

    @ModifyReturnValue(method = "isMovingSlowly", at = @At("RETURN"))
    public boolean isMovingSlowly(boolean original) {
        if (Psyop.MODULES.isActive(NoSlow.class)) {
            NoSlow module = Psyop.MODULES.get(NoSlow.class);

            if (module.sneaking.get()) {
                return false;
            }
        }

        return original;
    }
}
