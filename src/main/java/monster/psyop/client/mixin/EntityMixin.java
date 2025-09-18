package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.events.game.OnMove;
import monster.psyop.client.impl.modules.movement.AntiPush;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static monster.psyop.client.Liberty.MC;

@Mixin(value = Entity.class, priority = 777)
public abstract class EntityMixin {
    @Shadow
    public abstract int getId();

    @Inject(
            method = "push(DDD)V",
            at = @At("HEAD"),
            cancellable = true)
    public void push(double d, double e, double f, CallbackInfo ci) {
        if (getId() == MC.player.getId() && Liberty.MODULES.isActive(AntiPush.class)) {
            ci.cancel();
        }
    }

    /*@Redirect(
            method = "move",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(DDD)V"))
    public void onPhase(Entity instance, double d, double e, double f) {
        if (instance.getId() == Ribbets.MC.player.getId() && Ribbets.MODULES.isActive(Phase.class)) {
            Phase module = Ribbets.MODULES.get(Phase.class);

            if (instance.minorHorizontalCollision && module.ignoreMinor.get()) {
                instance.setDeltaMovement(d, module.applyVertical.get() ? e : instance.getDeltaMovement().y, f);
                return;
            }

            if (module.applyVertical.get()) {
                instance.setDeltaMovement(instance.getDeltaMovement().x, e, instance.getDeltaMovement().z);
            }
        } else {
            instance.setDeltaMovement(d, e, f);
        }
    }*/

    @Inject(
            method = "move",
            at = @At(value = "HEAD"))
    public void onMove(MoverType moverType, Vec3 vec3, CallbackInfo ci) {
        if ((Object) this == MC.player) {
            Liberty.EVENT_HANDLER.call(OnMove.Player.get(vec3, moverType));
        } else {
            Liberty.EVENT_HANDLER.call(OnMove.Entity.get(vec3, moverType, (Entity) (Object) this));
        }
    }
}
