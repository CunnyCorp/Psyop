package monster.psyop.client.mixin.anti_detection;

import monster.psyop.client.utility.spoofing.BlacklistedStrings;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// This is to patch a security exploit.
@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {
    @Redirect(method = "decompose", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getOrDefault(Ljava/lang/String;)Ljava/lang/String;"))
    public String decompose0(Language instance, String string) {
        if (BlacklistedStrings.isBlacklisted(string)) {
            return string;
        }

        return instance.getOrDefault(string);
    }

    @Redirect(method = "decompose", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    public String decompose1(Language instance, String s, String s2) {
        if (BlacklistedStrings.isBlacklisted(s)) {
            return s2;
        }

        return instance.getOrDefault(s, s2);
    }
}
