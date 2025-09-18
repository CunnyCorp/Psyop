package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.modules.movement.Jumping;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LivingEntity.class, priority = 777)
public abstract class LivingEntityMixin {
    @Shadow
    protected abstract float getJumpPower(float f);

    @Redirect(
            method = "getJumpPower()F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getJumpPower(F)F")
    )
    public float push(LivingEntity instance, float f) {
        if (Liberty.MODULES.isActive(Jumping.class)) {
            return getJumpPower(Liberty.MODULES.get(Jumping.class).jumpHeight.get());
        }

        return getJumpPower(f);
    }
}
