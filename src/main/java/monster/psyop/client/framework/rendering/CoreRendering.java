package monster.psyop.client.framework.rendering;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class CoreRendering {
    public static final RenderPipeline QUADS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.OVERLAY).withDepthWrite(false).build());
    public static final RenderPipeline LINES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET).withLocation("pipeline/lines").withCull(false).withDepthWrite(false).build());
    public static final RenderPipeline ENTITY_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET).withLocation("pipeline/entity_translucent").withShaderDefine("ALPHA_CUTOUT", 0.5f).withShaderDefine("EMISSIVE").withSampler("Sampler1").withBlend(BlendFunction.TRANSLUCENT).withCull(false).withDepthWrite(false).build());
    public static final RenderPipeline GLINT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.FOG_SNIPPET, RenderPipelines.LINES_SNIPPET).withLocation("pipeline/glint").withVertexShader("core/glint").withFragmentShader("core/glint").withSampler("Sampler0").withDepthWrite(false).withCull(false).withBlend(BlendFunction.GLINT).withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS).build());

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
    private static final Supplier<RenderType> GLINT_TRANSLUCENT = Suppliers.memoize(() ->
            RenderType.create("glint_translucent",
                    1536,
                    GLINT,
                    RenderType.CompositeState.builder()
                            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, false))
                            .setTexturingState(RenderType.GLINT_TEXTURING)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> ENTITY_GLINT = Suppliers.memoize(() ->
            RenderType.create("entity_glint",
                    1536,
                    GLINT,
                    RenderType.CompositeState.builder()
                            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ARMOR, false))
                            .setTexturingState(RenderType.ENTITY_GLINT_TEXTURING)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> GLINT_TRANSLUCENT_ENTITY = Suppliers.memoize(() ->
            RenderType.create("glint_translucent",
                    1536,
                    GLINT,
                    RenderType.CompositeState.builder()
                            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ARMOR, false))
                            .setTexturingState(RenderType.ENTITY_GLINT_TEXTURING)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );

    private static final Supplier<RenderType> WIREFRAME = Suppliers.memoize(() ->
            RenderType.create("wireframe",
                    1536,
                    RenderPipelines.WIREFRAME,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );

    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_TYPE = Util.memoize((resourceLocation, boolean_) -> {
        RenderType.CompositeState compositeState =
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false))
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setOutputState(RenderStateShard.OUTLINE_TARGET)
                        .createCompositeState(boolean_);
        return RenderType.create("entity_translucent", 1536, true, true, ENTITY_TRANSLUCENT, compositeState);
    });

    public static RenderType seeThroughLines() {
        return SEE_THROUGH_LINES_SUPPLIER.get();
    }

    public static RenderType seeThroughQuads() {
        return SEE_THROUGH_QUADS_SUPPLIER.get();
    }

    public static RenderType entityTranslucent(ResourceLocation resourceLocation, boolean bl) {
        return ENTITY_TRANSLUCENT_TYPE.apply(resourceLocation, bl);
    }

    public static RenderType glintTranslucent() {
        return GLINT_TRANSLUCENT.get();
    }

    public static RenderType entityGlint() {
        return ENTITY_GLINT.get();
    }

    public static RenderType wireframe() {
        return WIREFRAME.get();
    }

}
