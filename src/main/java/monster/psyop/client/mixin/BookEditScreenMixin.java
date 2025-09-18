package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.impl.events.game.OnBookSign;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin {
    @Shadow
    @Final
    private List<String> pages;

    @Inject(method = "saveChanges", at = @At("HEAD"), cancellable = true)
    public void onSaveChanges(CallbackInfo ci) {
        OnBookSign event = OnBookSign.get(pages);
        Liberty.EVENT_HANDLER.call(event);
        if (event.stop) {
            ci.cancel();
        } else {
            pages.clear();
            pages.addAll(event.pages);
        }
    }
}
