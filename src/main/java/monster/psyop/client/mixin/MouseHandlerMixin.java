package monster.psyop.client.mixin;

import monster.psyop.client.Liberty;
import monster.psyop.client.framework.gui.Gui;
import monster.psyop.client.impl.modules.misc.DetachMouse;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static monster.psyop.client.Liberty.MC;

@Mixin(value = MouseHandler.class, priority = 1)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Gui.IS_LOADED.get() && window == MC.getWindow().getWindow()) {
            ci.cancel();
        }
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (Gui.IS_LOADED.get() && window == MC.getWindow().getWindow()) {
            ci.cancel();
        }
    }

    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    public void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (Gui.IS_LOADED.get() && window == MC.getWindow().getWindow()) {
            ci.cancel();
        }
    }

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    public void grabMouse(CallbackInfo ci) {
        if (Gui.IS_LOADED.get() || Liberty.MODULES.isActive(DetachMouse.class)) {
            ci.cancel();
        }
    }
}
