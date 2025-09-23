package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.ImVec2;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.gui.GradientUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static monster.psyop.client.Psyop.GUI;

public class ArrayHUD extends HUD {
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

    private final List<String> labels = new ArrayList<>();
    private final GradientUtils gradientUtils = new GradientUtils(0.5f);

    public ArrayHUD() {
        super("array", "Shows a list of enabled modules.");
    }

    @EventListener(inGame = false)
    public void onTickPre(OnTick.Pre event) {
        gradientUtils.updateAnimation();

        labels.clear();

        for (Category cat : Categories.INDEX) {
            for (Module module : Psyop.MODULES.getModules(cat)) {
                if (module.active() && module.array.get()) {
                    String label = module.getLabel();

                    if (module.keybinding.get().get() != -1) {
                        label += " [" + KeyUtils.getTranslation(module.keybinding.get().get()) + "]";
                    }

                    labels.add(label);
                }
            }
        }

        labels.sort(Comparator.comparing(String::length).reversed());
    }

    @EventListener(inGame = false)
    public void render(On2DRender event) {
        int curY = yPos.get();

        Color[] waveColors = {
                GradientUtils.getColorFromSetting(upperColor),
                GradientUtils.getColorFromSetting(middleColor),
                GradientUtils.getColorFromSetting(lowerColor),
                GradientUtils.getColorFromSetting(middleColor)
        };

        for (String label : labels) {
            ImVec2 textSize = new ImVec2();
            ImGui.calcTextSize(textSize, label);

            float textX = xPos.get() - textSize.x;
            float bgX = textX - 4;
            float bgY = curY - 4;
            float bgWidth = textSize.x + 8;
            float bgHeight = textSize.y + 8;

            gradientUtils.drawHorizontalWaveGradientTile(bgX, bgY, bgWidth, bgHeight,
                    waveColors, alpha.get(), 2.0f, 0.5f);

            GUI.drawString(label, textX, curY, new ImColorW(textColor.get()));

            curY += (int) (textSize.y + 8);
        }
    }
}