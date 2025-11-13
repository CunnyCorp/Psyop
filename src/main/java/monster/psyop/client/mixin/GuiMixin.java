package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.events.game.OnVGuiRender;
import monster.psyop.client.impl.modules.render.RenderTweaks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Objects.equals(Psyop.MODULES.get(RenderTweaks.class).vanillaHudInjection.get(), "tail")) {
            Psyop.EVENT_HANDLER.call(OnVGuiRender.get(guiGraphics));
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSleepOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    public void render2(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Objects.equals(Psyop.MODULES.get(RenderTweaks.class).vanillaHudInjection.get(), "sleep")) {
            Psyop.EVENT_HANDLER.call(OnVGuiRender.get(guiGraphics));
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    public void render3(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Objects.equals(Psyop.MODULES.get(RenderTweaks.class).vanillaHudInjection.get(), "overlays")) {
            Psyop.EVENT_HANDLER.call(OnVGuiRender.get(guiGraphics));
        }
    }
}
