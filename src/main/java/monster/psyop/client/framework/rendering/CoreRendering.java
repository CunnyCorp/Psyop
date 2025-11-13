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
    public static final RenderPipeline QUADS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withDepthWrite(false).build());
    public static final RenderPipeline QUADS_OVERLAY = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.OVERLAY).withDepthWrite(false).build());
    public static final RenderPipeline QUADS_GLINT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.GLINT).withDepthWrite(false).build());
    public static final RenderPipeline QUADS_INVERT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.INVERT).withDepthWrite(false).build());
    public static final RenderPipeline QUADS_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).build());
    public static final RenderPipeline QUADS_LIGHTNING = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.LIGHTNING).withDepthWrite(false).build());
    public static final RenderPipeline QUADS_EOB = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.ENTITY_OUTLINE_BLIT).withDepthWrite(false).build());
    public static final RenderPipeline QUADS_ADD = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.ADDITIVE).withDepthWrite(false).build());
    public static final RenderPipeline QUADS_TPMA = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation("pipeline/debug_quads").withCull(false).withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA).withDepthWrite(false).build());

    public static final RenderPipeline LINES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET).withLocation("pipeline/lines").withCull(false).withDepthWrite(false).build());
    public static final RenderPipeline ENTITY_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET).withLocation("pipeline/entity_translucent").withShaderDefine("ALPHA_CUTOUT", 0.5f).withShaderDefine("EMISSIVE").withSampler("Sampler1").withBlend(BlendFunction.TRANSLUCENT).withCull(false).withDepthWrite(false).build());
    public static final RenderPipeline GLINT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.FOG_SNIPPET, RenderPipelines.LINES_SNIPPET).withLocation("pipeline/glint").withVertexShader("core/glint").withFragmentShader("core/glint").withSampler("Sampler0").withDepthWrite(false).withCull(false).withBlend(BlendFunction.GLINT).withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS).build());

    private static final Supplier<RenderType> LINES_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_lines",
                    1536, LINES,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads",
                    1536,
                    QUADS,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_GLINT_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads_glint",
                    1536,
                    QUADS_GLINT,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_OVERLAY_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads_overlay",
                    1536,
                    QUADS_OVERLAY,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_INVERT_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads_invert",
                    1536,
                    QUADS_INVERT,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_ADD_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads_add",
                    1536,
                    QUADS_ADD,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_EOB_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads_eob",
                    1536,
                    QUADS_EOB,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_LIGHTNING_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads_lightning",
                    1536,
                    QUADS_LIGHTNING,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_TPMA_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads_tpma",
                    1536,
                    QUADS_TPMA,
                    RenderType.CompositeState.builder()
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                            .setOutputState(RenderStateShard.OUTLINE_TARGET)
                            .createCompositeState(false)
            )
    );
    private static final Supplier<RenderType> QUADS_TRANSLUCENT_SUPPLIER = Suppliers.memoize(() ->
            RenderType.create(
                    "psyop_quads_translucent",
                    1536,
                    QUADS_TRANSLUCENT,
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
                            .setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, false))
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
                        .setTexturingState(RenderType.ENTITY_GLINT_TEXTURING)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setOutputState(RenderStateShard.OUTLINE_TARGET)
                        .createCompositeState(boolean_);
        return RenderType.create("entity_translucent", 1536, true, true, ENTITY_TRANSLUCENT, compositeState);
    });

    public static RenderType lines() {
        return LINES_SUPPLIER.get();
    }

    public static RenderType quads() {
        return QUADS_SUPPLIER.get();
    }

    public static RenderType quadsGlint() {
        return QUADS_GLINT_SUPPLIER.get();
    }

    public static RenderType quadsOverlay() {
        return QUADS_OVERLAY_SUPPLIER.get();
    }

    public static RenderType quadsInvert() {
        return QUADS_INVERT_SUPPLIER.get();
    }

    public static RenderType quadsAdd() {
        return QUADS_ADD_SUPPLIER.get();
    }

    public static RenderType quadsEOB() {
        return QUADS_EOB_SUPPLIER.get();
    }

    public static RenderType quadsLightning() {
        return QUADS_LIGHTNING_SUPPLIER.get();
    }

    public static RenderType quadsTPMA() {
        return QUADS_TPMA_SUPPLIER.get();
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
