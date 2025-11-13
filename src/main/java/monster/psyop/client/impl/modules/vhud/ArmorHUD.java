package monster.psyop.client.impl.modules.vhud;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnVGuiRender;
import monster.psyop.client.impl.modules.hud.HUD;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorHUD extends HUD {
    public EquipmentSlot[] equipmentSlots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public EquipmentSlot[] reverseEquipmentSlots = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public IntSetting offset = new IntSetting.Builder()
            .name("offset-debug")
            .defaultTo(16)
            .range(0, 24)
            .addTo(coreGroup);
    public BoolSetting reverse = new BoolSetting.Builder()
            .name("reverse")
            .defaultTo(false)
            .addTo(coreGroup);
    public FloatSetting scale = new FloatSetting.Builder()
            .name("scale")
            .defaultTo(1.0f)
            .range(0.3f, 2.0f)
            .addTo(coreGroup);


    public ArmorHUD() {
        super("armor", "Displays equipped armor items!");
    }

    @EventListener
    public void onRender(OnVGuiRender event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();

        EquipmentSlot[] es = reverse.get() ? reverseEquipmentSlots : equipmentSlots;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale.get());

        for (int i = 0; i < es.length; i++) {
            EquipmentSlot slot = es[i];
            if (MC.player != null && !MC.player.getItemBySlot(slot).isEmpty()) {
                ItemStack stack = MC.player.getItemBySlot(slot);
                guiGraphics.renderItem(stack, getVanillaX() + (offset.get() * i), getVanillaY());
                guiGraphics.renderItemDecorations(MC.font, stack, getVanillaX() + (offset.get() * i), getVanillaY());
            }
        }

        guiGraphics.pose().popMatrix();
    }

    @Override
    public int getWidth() {
        return (int) (((offset.get() * 4) * MC.options.guiScale().get()) * scale.get());
    }

    @Override
    public int getHeight() {
        return (int) (((offset.get()) * MC.options.guiScale().get()) * scale.get());
    }
}
