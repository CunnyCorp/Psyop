package monster.psyop.client.impl.modules.vhud;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnVGuiRender;
import monster.psyop.client.impl.modules.hud.HUD;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class InventoryHUD extends HUD {
    public IntSetting offset = new IntSetting.Builder()
            .name("offset-debug")
            .defaultTo(16)
            .range(0, 24)
            .addTo(coreGroup);
    public IntSetting yOffset = new IntSetting.Builder()
            .name("y-offset-debug")
            .defaultTo(16)
            .range(0, 24)
            .addTo(coreGroup);
    public FloatSetting scale = new FloatSetting.Builder()
            .name("scale")
            .defaultTo(1.0f)
            .range(0.3f, 2.0f)
            .addTo(coreGroup);

    public InventoryHUD() {
        super("inventory", "Displays your inventory as a hud element!");
    }

    @EventListener
    public void onRender(OnVGuiRender event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale.get());

        int slot = 0;
        int row = 0;

        for (ItemStack stack : MC.player.getInventory().getNonEquipmentItems()) {
            if (slot >= 9) {
                slot = 0;
                row++;
            }

            if (row == 0) {
                slot++;
                continue;
            }

            guiGraphics.renderItem(stack, getVanillaX() + (offset.get() * slot), getVanillaY() + (offset.get() * row));
            guiGraphics.renderItemDecorations(MC.font, stack, getVanillaX() + (offset.get() * slot), getVanillaY() + (offset.get() * row));

            slot++;
        }

        guiGraphics.pose().popMatrix();
    }

    @Override
    public int getWidth() {
        return (int) (((offset.get() * 9) * MC.options.guiScale().get()) * scale.get());
    }

    @Override
    public int getHeight() {
        return (int) (((yOffset.get() * 3) * MC.options.guiScale().get()) * scale.get());
    }
}
