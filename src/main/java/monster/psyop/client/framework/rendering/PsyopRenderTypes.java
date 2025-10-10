package monster.psyop.client.framework.rendering;

import com.google.common.base.Suppliers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * Custom RenderTypes for Psyop client.
 * Provides RenderTypes that render to the OUTLINE_TARGET (no depth),
 * guaranteeing visibility through walls.
 */
public final class PsyopRenderTypes {
    private PsyopRenderTypes() {
    }

    // Lazily create to avoid class init order issues with RenderSystem/MC bootstrap
    private static final Supplier<RenderType> SEE_THROUGH_LINES_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_see_through_lines",
                    1536,
                    RenderPipelines.LINES,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                            .setLayeringState(RenderStateShard.NO_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET) // outline buffer has no depth -> see-through
                            .createCompositeState(true)
            )
    );

    private static final Supplier<RenderType> SEE_THROUGH_QUADS_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_see_through_quads",
                    1536,
                    RenderPipelines.DEBUG_QUADS,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.NO_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(true)
            )
    );

    public static RenderType seeThroughLines() {
        return SEE_THROUGH_LINES_SUPPLIER.get();
    }

    public static RenderType seeThroughQuads() {
        return SEE_THROUGH_QUADS_SUPPLIER.get();
    }
}
