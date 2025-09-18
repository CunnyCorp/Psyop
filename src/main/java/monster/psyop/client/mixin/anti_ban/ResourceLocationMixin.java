package monster.psyop.client.mixin.anti_ban;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ResourceLocation.class, priority = 373017)
public class ResourceLocationMixin {
    /**
     * @author Rosa
     * @reason Prevent invalid name spaces from kicking the player.
     */
    @Overwrite
    public static boolean isValidNamespace(String string) {
        return true;
    }
}
