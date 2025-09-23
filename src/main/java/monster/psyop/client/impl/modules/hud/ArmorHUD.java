package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.ImVec2;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.gui.GradientUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;

import java.awt.*;

import static monster.psyop.client.Psyop.GUI;

public class ArmorHUD extends HUD {
    public ColorSetting textColor =
            new ColorSetting.Builder()
                    .name("text-color")
                    .defaultTo(new float[]{0.90f, 0.90f, 0.95f, 0.95f})
                    .addTo(coreGroup);
    public ColorSetting upperColor =
            new ColorSetting.Builder()
                    .name("upper-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public ColorSetting middleColor =
            new ColorSetting.Builder()
                    .name("middle-color")
                    .defaultTo(new float[]{0.00f, 0.60f, 0.60f, 1.0f})
                    .addTo(coreGroup);
    public ColorSetting lowerColor =
            new ColorSetting.Builder()
                    .name("lower-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public final IntSetting alpha =
            new IntSetting.Builder()
                    .name("alpha")
                    .range(40, 200)
                    .defaultTo(184)
                    .addTo(coreGroup);

    public EquipmentSlot[] equipmentSlots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private final GradientUtils gradientUtils = new GradientUtils(0.5f);

    public ArmorHUD() {
        super("ArmorHUD", "Shows the durability of equipped armor.");
    }

    @EventListener(inGame = false)
    public void onTickPre(OnTick.Pre event) {
        gradientUtils.updateAnimation();
    }

    @EventListener(inGame = false)
    public void render(On2DRender event) {
        float yP = yPos.get();

        Color[] waveColors = {
                GradientUtils.getColorFromSetting(upperColor),
                GradientUtils.getColorFromSetting(middleColor),
                GradientUtils.getColorFromSetting(lowerColor),
                GradientUtils.getColorFromSetting(middleColor)
        };

        for (int i = 0; i < equipmentSlots.length; i++) {
            EquipmentSlot slot = equipmentSlots[i];

            if (MC.player == null || MC.player.getItemBySlot(slot).isEmpty() || !MC.player.getItemBySlot(slot).has(DataComponents.MAX_DAMAGE)) {
                String emptyText = "0/0";
                ImVec2 textSize = new ImVec2();
                ImGui.calcTextSize(textSize, emptyText);

                float bgX = xPos.get() - 4;
                float bgY = yP - 4;
                float bgWidth = textSize.x + 8;
                float bgHeight = textSize.y + 8;

                gradientUtils.drawHorizontalWaveGradientTile(bgX, bgY, bgWidth, bgHeight,
                        waveColors, alpha.get(), 2.0f, 0.5f);

                GUI.drawString(emptyText, xPos.get(), yP, new ImColorW(textColor.get()).packed());
                yP += textSize.y + 8;
            } else {
                int durability = MC.player.getItemBySlot(slot).getMaxDamage() - MC.player.getItemBySlot(slot).getDamageValue();
                String durabilityText = durability + "/" + MC.player.getItemBySlot(slot).getMaxDamage();

                ImVec2 textSize = new ImVec2();
                ImGui.calcTextSize(textSize, durabilityText);

                float bgX = xPos.get() - 4;
                float bgY = yP - 4;
                float bgWidth = textSize.x + 8;
                float bgHeight = textSize.y + 8;

                gradientUtils.drawHorizontalWaveGradientTile(bgX, bgY, bgWidth, bgHeight,
                        waveColors, alpha.get(), 2.0f, 0.5f);

                GUI.drawString(durabilityText, xPos.get(), yP, new ImColorW(textColor.get()).packed());
                yP += textSize.y + 8;
            }
        }
    }
}