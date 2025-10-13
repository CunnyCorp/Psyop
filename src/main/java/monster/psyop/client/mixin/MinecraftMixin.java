package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.events.game.OnScreen;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.impl.modules.misc.NoMiss;
import monster.psyop.client.impl.modules.player.AutoEat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 777)
public class MinecraftMixin {
    @Shadow
    protected int missTime;
    @Unique
    private double lastUse = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        Psyop.postInit();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickHead(CallbackInfo ci) {
        Psyop.EVENT_HANDLER.call(OnTick.Pre.get());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickTail(CallbackInfo ci) {
        Psyop.EVENT_HANDLER.call(OnTick.Post.get());
    }

    @Inject(
            method = "setScreen",
            at = @At("HEAD"),
            cancellable = true)
    public void setScreen(@Nullable Screen screen, CallbackInfo ci) {
        OnScreen.Open event = OnScreen.Open.get(screen);
        Psyop.EVENT_HANDLER.call(event);
        if (event.isCancelled()) {
            Psyop.log("Closing screen with a event.");
            ci.cancel();
        }
    }

    @Inject(
            method = "runTick",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateDisplay(Lcom/mojang/blaze3d/TracyFrameCapture;)V", shift = At.Shift.BEFORE))
    public void runTickTail(boolean bl, CallbackInfo ci) {
        if (Psyop.GUI != null) {
            Psyop.GUI.renderFrame();
            double currentUse = Math.abs(System.currentTimeMillis() / 1000);
            if (currentUse != lastUse) {
                if (Psyop.DEBUGGING) System.out.println("Still trying to render frame.");
                lastUse = currentUse;
            }
        }
    }

    @Inject(
            method = "continueAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"),
            cancellable = true)
    public void continueAttack0(boolean bl, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(NoMiss.class)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "startUseItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"))
    public boolean startUseItem(MultiPlayerGameMode instance) {
        if (Psyop.MODULES.isActive(NoMiss.class)) {
            return false;
        }

        return instance.isDestroying();
    }

    @Inject(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"),
            cancellable = true)
    public void handleKeybinds(CallbackInfo ci) {
        if (Psyop.MODULES.isActive(AutoEat.class) && Psyop.MODULES.get(AutoEat.class).inUse()) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    public boolean handleKeybinds2(KeyMapping instance) {
        if (Psyop.MODULES.isActive(AutoEat.class) && Psyop.MODULES.get(AutoEat.class).inUse() && instance == Psyop.MC.options.keyUse) {
            return true;
        }

        return instance.isDown();
    }
}
