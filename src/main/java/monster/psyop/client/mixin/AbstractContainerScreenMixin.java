package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.events.game.OnRenderSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Redirect(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"))
    public void renderSlot(GuiGraphics guiGraphics, ItemStack st, int x, int y, int k) {
        OnRenderSlot.Pre event = OnRenderSlot.Pre.get(guiGraphics, st, x, y);

        Psyop.EVENT_HANDLER.call(event);

        if (event.isCancelled()) {
            return;
        }

        // I genuinely don't know what k does
        guiGraphics.renderItem(st, x, y, k);

        Psyop.EVENT_HANDLER.call(OnRenderSlot.Post.get(guiGraphics, st, x, y));
    }
}
