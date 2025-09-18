package monster.psyop.client.mixin;

import net.minecraft.client.gui.font.TextFieldHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(value = TextFieldHelper.class, priority = 777)
public class TextFieldHelperMixin {
    @Redirect(
            method = "insertText(Ljava/lang/String;Ljava/lang/String;)V",
            at =
            @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
    public boolean insertTextTest(Predicate<?> instance, Object t) {
        return true;
    }
}
