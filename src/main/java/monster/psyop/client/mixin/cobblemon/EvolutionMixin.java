package monster.psyop.client.mixin.cobblemon;

import com.cobblemon.mod.common.api.pokemon.evolution.Evolution;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Evolution.class, remap = false)
public abstract class EvolutionMixin {
    /**
     * @author rosa1n
     * @reason Spoof evolution readiness OwO
     */
    @Overwrite
    public boolean test(Pokemon par1) {
        return true;
    }
}
