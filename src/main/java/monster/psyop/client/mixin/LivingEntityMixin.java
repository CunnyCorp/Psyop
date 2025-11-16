package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.movement.Jumping;
import net.minecraft.util.Mth;
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
        if (Psyop.MODULES.isActive(Jumping.class)) {
            return getJumpPower(Psyop.MODULES.get(Jumping.class).jumpHeight.get());
        }

        return getJumpPower(f);
    }

    @Redirect(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(F)F"))
    public float jumpFromGround0(float f) {
        if (Psyop.MODULES.isActive(Jumping.class)) {
            return Mth.sin(f * Psyop.MODULES.get(Jumping.class).jumpSprintMulti.get());
        }

        return Mth.sin(f);
    }

    @Redirect(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(F)F"))
    public float jumpFromGround1(float f) {
        if (Psyop.MODULES.isActive(Jumping.class)) {
            return Mth.cos(f * Psyop.MODULES.get(Jumping.class).jumpSprintMulti.get());
        }

        return Mth.cos(f);
    }
}
