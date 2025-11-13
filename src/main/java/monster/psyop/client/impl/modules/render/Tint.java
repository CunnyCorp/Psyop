package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.impl.events.game.OnVGuiRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;

public class Tint extends Module {
    public ColorSetting color = new ColorSetting.Builder()
            .name("tint-color")
            .defaultTo(new float[]{0.0f, 0.2f, 0.84f, 0.2f})
            .addTo(coreGroup);

    public Tint() {
        super(Categories.RENDER, "tint", "Apply a tint to the screen.");
    }

    @EventListener
    public void onVGuiRender(OnVGuiRender event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();

        guiGraphics.fill(
                0, 0,
                guiGraphics.guiWidth(), guiGraphics.guiHeight(),
                ARGB.color((int) (color.get()[3] * 255), (int) (color.get()[0] * 255), (int) (color.get()[1] * 255), (int) (color.get()[2] * 255)));
    }
}
