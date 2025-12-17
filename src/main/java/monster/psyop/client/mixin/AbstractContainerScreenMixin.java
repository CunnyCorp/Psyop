package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.vhud.BetterShulkers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Redirect(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"))
    public void renderSlot(GuiGraphics guiGraphics, ItemStack st, int x, int y, int k) {
        // I genuinely don't know what k does
        guiGraphics.renderItem(st, x, y, k);

        if (Psyop.MODULES.isActive(BetterShulkers.class)) {

            BetterShulkers module = Psyop.MODULES.get(BetterShulkers.class);

            if (st.has(DataComponents.CONTAINER)) {
                ItemContainerContents contents = st.get(DataComponents.CONTAINER);

                if (contents != null) {
                    Item majorityItem = module.getMajorityItem(contents);

                    if (majorityItem != Items.AIR) {
                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().scale(module.miScale.get());

                        float inverseScale = 1.0f / module.miScale.get();;
                        float scaledX = (x + module.xOffsetMI.get()) * inverseScale;
                        float scaledY = (y + module.yOffsetMI.get()) * inverseScale;

                        guiGraphics.renderItem(majorityItem.getDefaultInstance(), (int) scaledX, (int) scaledY);
                        guiGraphics.pose().popMatrix();
                    }
                }
            }
        }
    }
}
