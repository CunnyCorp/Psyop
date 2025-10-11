package monster.psyop.client.framework.rendering;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;
import java.util.function.Supplier;

public final class PsyopRenderTypes {
    public static final RenderPipeline QUADS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withDepthWrite(false).build());
    public static final RenderPipeline LINES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET).withLocation("pipeline/lines").withCull(false).withDepthWrite(false).build());
    private static final Supplier<RenderType> SEE_THROUGH_LINES_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_see_through_lines",
                    1536, LINES,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> SEE_THROUGH_QUADS_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_see_through_quads",
                    1536,
                    QUADS,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );

    public static RenderType seeThroughLines() {
        return SEE_THROUGH_LINES_SUPPLIER.get();
    }

    public static RenderType seeThroughQuads() {
        return SEE_THROUGH_QUADS_SUPPLIER.get();
    }
}
