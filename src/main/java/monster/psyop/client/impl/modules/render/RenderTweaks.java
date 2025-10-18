package monster.psyop.client.impl.modules.render;

import imgui.type.ImString;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.ProvidedStringSetting;
import monster.psyop.client.framework.rendering.CoreRendering;
import net.minecraft.client.renderer.RenderType;

import java.util.List;

public class RenderTweaks extends Module {
    public ProvidedStringSetting quadsModifier = new ProvidedStringSetting.Builder()
            .suggestions(List.of(new ImString("quads"), new ImString("quads_invert"), new ImString("quads_overlay"), new ImString("quads_glint"), new ImString("quads_additive"), new ImString("quads_tpma"), new ImString("quads_eob"), new ImString("quads_lightning")))
            .name("quads-modifier")
            .defaultTo(new ImString("quads"))
            .addTo(coreGroup);

    public RenderTweaks() {
        super(Categories.RENDER, "render-tweaks", "Lets you modify how Psyop renders, always active.");
    }

    public static RenderType getQuadsRenderType() {
        RenderTweaks module = Psyop.MODULES.get(RenderTweaks.class);

        return switch (module.quadsModifier.get().get()) {
            case "quads_invert" -> CoreRendering.quadsInvert();
            case "quads_overlay" -> CoreRendering.quadsOverlay();
            case "quads_glint" -> CoreRendering.quadsGlint();
            case "quads_additive" -> CoreRendering.quadsAdd();
            case "quads_tpma" -> CoreRendering.quadsTPMA();
            case "quads_eob" -> CoreRendering.quadsEOB();
            case "quads_lightning" -> CoreRendering.quadsLightning();
            default -> CoreRendering.quads();
        };
    }
}
