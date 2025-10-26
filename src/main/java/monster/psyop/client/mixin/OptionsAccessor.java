package monster.psyop.client.mixin;

import net.minecraft.client.Options;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(Options.class)
public interface OptionsAccessor {
    @Accessor("modelParts")
    Set<PlayerModelPart> getModelParts();
}
