package monster.psyop.client.mixin.norender;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.render.NoRender;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    public void renderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(NoRender.class)) {
            NoRender module = Psyop.MODULES.get(NoRender.class);

            if (module.playerStatus.get()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    public void renderHearts(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(NoRender.class)) {
            NoRender module = Psyop.MODULES.get(NoRender.class);

            if (module.healthBar.get()) {
                ci.cancel();
            }
        }
    }


    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    public void renderFood(GuiGraphics guiGraphics, Player player, int i, int j, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(NoRender.class)) {
            NoRender module = Psyop.MODULES.get(NoRender.class);

            if (module.healthBar.get()) {
                ci.cancel();
            }
        }
    }
}
