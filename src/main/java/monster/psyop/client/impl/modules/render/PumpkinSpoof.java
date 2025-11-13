package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.impl.events.game.OnVGuiRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class PumpkinSpoof extends Module {
    public ColorSetting color = new ColorSetting.Builder()
            .name("color")
            .defaultTo(new float[]{0.0f, 0.0f, 0.0f, 0.6f})
            .addTo(coreGroup);

    public PumpkinSpoof() {
        super(Categories.RENDER, "pumpkin-spoof", "Spoofs a fake pumpkin overlay!");
    }

    @EventListener
    public void onRender(OnVGuiRender event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ResourceLocation.withDefaultNamespace("textures/misc/pumpkinblur.png"),
                0,
                0,
                0.0f,
                0.0f,
                event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(),
                event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(),
                ARGB.color((int) (color.get()[3] * 255), (int) (color.get()[0] * 255), (int) (color.get()[1] * 255), (int) (color.get()[2] * 255)));
    }
}
