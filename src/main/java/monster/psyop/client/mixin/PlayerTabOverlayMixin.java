package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.modules.render.BetterTab;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

/**
 * Replaces the server name part of the tab header with "psyop" and draws it with a blue->purple gradient.
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    // shadow the private header field so we can set it directly
    @Shadow @Nullable private Component header;

    @Inject(method = "setHeader", at = @At("HEAD"), cancellable = true)
    private void onSetHeader(@Nullable Component newHeader, CallbackInfo ci) {
        BetterTab betterTab = Psyop.MODULES.get(BetterTab.class);
        if (betterTab == null || !betterTab.active() || !betterTab.customTab.get()) return;

        if (newHeader == null) return;

        String raw = newHeader.getString();

        // find the " - " separator commonly found in "SERVER - Current players: X"
        int hyphenIndex = raw.indexOf(" - ");
        String rest = hyphenIndex >= 0 ? raw.substring(hyphenIndex) : raw; // keep the hyphen and everything after
        // Build new header: gradient "psyop" + the rest of the original string
        MutableComponent out = Component.empty();
        out.append(Component.literal("\n"));
        out.append(gradientText("psyop", new Color(0, 150, 255), new Color(170, 0, 255)));

        if (!rest.isEmpty()) {
            out.append(Component.literal(rest).withStyle(Style.EMPTY));
        }

        // set the shadowed field and cancel original setter so it doesn't overwrite our value
        this.header = out;
        ci.cancel();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getBackgroundColor(I)I"))
    public int getBackgroundColor(Options instance, int i) {
        if (Psyop.MODULES.isActive(BetterTab.class)) {
            return ImColorW.packed(Psyop.MODULES.get(BetterTab.class).customColor.get());
        }

        return instance.getBackgroundColor(i);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    public void getBackgroundColor(GuiGraphics instance, int i, int j, int k, int l, int m) {
        if (Psyop.MODULES.isActive(BetterTab.class)) {
            instance.fill(i, j, k, l, ImColorW.packed(Psyop.MODULES.get(BetterTab.class).customColor2.get()));
            return;
        }

        instance.fill(i, j, k, l, m);
    }

    @Unique
    private MutableComponent gradientText(String text, Color start, Color end) {
        MutableComponent comp = Component.empty();
        if (text == null || text.isEmpty()) return comp;

        int len = text.length();
        for (int i = 0; i < len; i++) {
            float ratio = (len == 1) ? 0f : (float) i / (len - 1);
            int r = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int g = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int b = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));
            int rgb = new Color(r, g, b).getRGB();

            comp.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(rgb)));
        }
        return comp;
    }
}
