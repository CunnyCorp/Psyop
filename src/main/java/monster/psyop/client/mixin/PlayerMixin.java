package monster.psyop.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.movement.Speed;
import monster.psyop.client.impl.modules.player.Reach;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Player.class, priority = 777)
public class PlayerMixin {
    @ModifyReturnValue(method = "blockInteractionRange", at = @At("RETURN"))
    public double blockInteractionRange(double original) {
        if (Psyop.MODULES.isActive(Reach.class)) {
            Reach module = Psyop.MODULES.get(Reach.class);

            return original + (double) module.block.get();
        }

        return original;
    }

    @ModifyReturnValue(method = "entityInteractionRange", at = @At("RETURN"))
    public double entityInteractionRange(double original) {
        if (Psyop.MODULES.isActive(Reach.class)) {
            Reach module = Psyop.MODULES.get(Reach.class);

            return original + (double) module.entity.get();
        }

        return original;
    }

    @ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
    public float getSpeed(float original) {
        if (Psyop.MODULES.isActive(Speed.class)) {
            Speed module = Psyop.MODULES.get(Speed.class);

            if (module.attribute.get()) {
                float speed = original;

                speed += module.speedBase.get();
                speed *= module.speedMulti.get();

                return speed;
            }
        }

        return original;
    }
}
