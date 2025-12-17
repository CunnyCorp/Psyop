package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.font.GlyphInfo;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnChunk;
import monster.psyop.client.impl.events.game.OnMove;
import monster.psyop.client.impl.events.game.OnVGuiRender;
import monster.psyop.client.utility.TextUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3f;

public class BetterSigns extends Module {
    public GroupedSettings farSignGroup = addGroup(new GroupedSettings("far-sign", "Special thanks to fraze <3"));
    public GroupedSettings loggerGroup = addGroup(new GroupedSettings("logger", "Log sign text!"));

    public final BoolSetting farSign = new BoolSetting.Builder()
            .name("far-sign")
            .defaultTo(true)
            .addTo(farSignGroup);
    public final FloatSetting scale = new FloatSetting.Builder()
            .name("scale")
            .defaultTo(0.7f)
            .range(0.3f, 1.0f)
            .addTo(farSignGroup);
    public final FloatSetting castDistance = new FloatSetting.Builder()
            .name("cast-distance")
            .defaultTo(16.0f)
            .range(8, 72)
            .addTo(farSignGroup);
    public final IntSetting xPos = new IntSetting.Builder()
            .name("x")
            .defaultTo(100)
            .range(0, 4000)
            .addTo(farSignGroup);
    public final IntSetting yPos = new IntSetting.Builder()
            .name("y")
            .defaultTo(100)
            .range(0, 4000)
            .addTo(farSignGroup);
    public final BoolSetting bothSides = new BoolSetting.Builder()
            .name("both-sides")
            .description("Will use x and y for front, x-b and y-b for back.")
            .defaultTo(false)
            .addTo(farSignGroup);
    public final IntSetting xBPos = new IntSetting.Builder()
            .name("x-b")
            .defaultTo(100)
            .range(0, 4000)
            .addTo(farSignGroup);
    public final IntSetting yBPos = new IntSetting.Builder()
            .name("y-b")
            .defaultTo(100)
            .range(0, 4000)
            .addTo(farSignGroup);

    public final BoolSetting logger = new BoolSetting.Builder()
            .name("logger")
            .description("Logs sign text to chat.")
            .defaultTo(false)
            .addTo(loggerGroup);
    public final IntSetting maxLogged = new IntSetting.Builder()
            .name("max-logged")
            .defaultTo(8)
            .range(1, 254)
            .addTo(loggerGroup);
    public final BoolSetting streamerMode = new BoolSetting.Builder()
            .name("streamer-mode")
            .defaultTo(false)
            .addTo(loggerGroup);


    private SignBlockEntity blockEntity;

    public BetterSigns() {
        super(Categories.RENDER, "better-signs", "TwoHax Skidware omg! - Love Fraze my pooks <3");
    }

    @EventListener
    public void onRenderVGui(OnVGuiRender event) {
        GuiGraphics graphics = event.getGuiGraphics();

        if (farSign.get() && blockEntity != null) {
            if (bothSides.get()) {
                sign(graphics, blockEntity, true, getVanillaX(xPos.get()), getVanillaY(yPos.get()), scale.get());
                sign(graphics, blockEntity, false, getVanillaX(xBPos.get()), getVanillaY(yBPos.get()), scale.get());
            } else {
                sign(graphics, blockEntity, true, getVanillaX(xPos.get()), getVanillaY(yPos.get()), scale.get());
            }
        }
    }

    @EventListener
    public void onChunkLoad(OnChunk.Load event) {
        if (!logger.get()) {
            return;
        }

        final int[] signsLogged = {0};

        MC.level.getChunk(event.x, event.z).getBlockEntities().forEach((blockPos, be) -> {
            if (be instanceof SignBlockEntity sign) {
                if (signsLogged[0] >= maxLogged.get()) {
                    signsLogged[0]++;
                    return;
                }

                signsLogged[0]++;

                MutableComponent coords = Component.literal("Sign: ");

                coords.setStyle(TextUtils.MODULE_NAME_STYLE);

                coords.append(Component.literal(streamerMode.get() ? "[SECRET]" : blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ()).setStyle(TextUtils.MODULE_INFO_STYLE));

                MC.gui.getChat().addMessage(coords);

                int emptyCount = 0;

                if (sign.getFrontText().hasMessage(MC.player)) {
                    for (Component text : sign.getFrontText().getMessages(false)) {
                        MutableComponent signLine = Component.literal(text.getString());

                        signLine.setStyle(Style.EMPTY.withBold(sign.getFrontText().hasGlowingText()).withColor(noBlack(sign.getFrontText().getColor()).getTextColor()));

                        MC.gui.getChat().addMessage(signLine);
                    }
                } else {
                    emptyCount++;
                }

                if (sign.getBackText().hasMessage(MC.player)) {
                    for (Component text : sign.getBackText().getMessages(false)) {
                        MutableComponent signLine = Component.literal(text.getString());

                        signLine.setStyle(Style.EMPTY.withBold(sign.getBackText().hasGlowingText()).withColor(noBlack(sign.getBackText().getColor()).getTextColor()));

                        MC.gui.getChat().addMessage(signLine);
                    }
                } else {
                    emptyCount++;
                }

                if (emptyCount == 2) {
                    MutableComponent component = Component.empty();
                    component.append(TextUtils.BRACKET_OPEN_COMPONENT);
                    component.append(TextUtils.CLIENT_TITLE);
                    component.append(TextUtils.BRACKET_CLOSE_COMPONENT);
                    component.append(Component.literal("The sign was empty!").withStyle(TextUtils.MODULE_INFO_STYLE));

                    MC.gui.getChat().addMessage(component);
                }
            }
        });

        if (signsLogged[0] > 0) {
            MutableComponent component = Component.empty();
            component.append(TextUtils.BRACKET_OPEN_COMPONENT);
            component.append(TextUtils.CLIENT_TITLE);
            component.append(TextUtils.BRACKET_CLOSE_COMPONENT);
            component.append(Component.literal(signsLogged[0] + " ").withStyle(TextUtils.MODULE_INFO_SUB_STYLE));
            component.append(Component.literal("signs logged in chunk: ").withStyle(TextUtils.MODULE_INFO_STYLE));
            component.append(Component.literal(streamerMode.get() ? "[SECRET]" : event.x + ", " + event.z).withStyle(TextUtils.MODULE_INFO_STYLE));

            MC.gui.getChat().addMessage(component);
        }
    }

    @EventListener
    public void onPlayerMove(OnMove.Player event) {
        if (MC.player == null) return;

        this.blockEntity = null;
        if (MC.cameraEntity != null && MC.level != null) {
            HitResult result = MC.gameRenderer.pick(
                    MC.cameraEntity,
                    (double) this.castDistance.get(),
                    (double) this.castDistance.get(),
                    1F
            );

            if (result.getType() == HitResult.Type.BLOCK) {
                BlockEntity tileEntity = MC.level.getBlockEntity(BlockPos.containing(result.getLocation()));
                if(tileEntity instanceof SignBlockEntity sign) {
                    this.blockEntity = sign;
                }
            }
        }
    }

    public DyeColor noBlack(DyeColor dyeColor) {
        if (dyeColor == DyeColor.BLACK) {
            return DyeColor.MAGENTA;
        }

        return dyeColor;
    }


    // I censored the curse words from Fraze's comments -Vali
    /**
     * Draws a sign (without the "stick" or "chain" parts).
     * @param ctx the relevant {@link net.minecraft.client.gui.GuiGraphics}
     * @param sign the {@link SignBlockEntity} to draw
     * @param front whether to draw the front or the back
     * @param x coordinate on the x-axis to draw at
     * @param y coordinate on the y-axis to draw at
     * @param scale the scaling multiplier
     */
    public static void sign(GuiGraphics ctx, SignBlockEntity sign, boolean front, int x, int y, float scale) {
        Block signBlock = sign.getBlockState().getBlock();
        WoodType woodType = SignBlock.getWoodType(signBlock);
        boolean isHanging = signBlock instanceof CeilingHangingSignBlock ||
                signBlock instanceof WallHangingSignBlock;

        Model model;
        if (isHanging) {
            model = HangingSignRenderer.createSignModel(
                    MC.getEntityModels(),
                    woodType,
                    HangingSignRenderer.AttachmentType.WALL
            );
        } else {
            model = SignRenderer.createSignModel(
                    MC.getEntityModels(),
                    woodType,
                    false
            );
        }

        Matrix3x2fStack matrices = ctx.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(scale);
        matrices.pushMatrix();

        if (isHanging) {
            // yes so basically mojang converted normal sign editor to model only
            // but left the hanging sign one as texture because they couldn't be f*cked i guess
            // these people are maniacs (watch them finish the job next version and f*ck this again)
            matrices.scale(HangingSignEditScreen.MAGIC_BACKGROUND_SCALE);
            ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/gui/hanging_signs/" + woodType.name() + ".png");
            int textureWidth = 16; // IT JUST IS, OKAY?!
            int textureHeight = 16;
            int cropHeight = 6;
            int drawHeight = textureHeight - cropHeight;
            ctx.blit(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    -(textureWidth / 2),
                    0,
                    0.0F,
                    cropHeight,
                    textureWidth,
                    drawHeight,
                    textureWidth,
                    textureHeight
            );
        } else {
            int x1 = x - (int) (48 * scale);
            int x2 = x + (int) (48 * scale);
            int y2 = y + (int) (102 * scale);
            ctx.submitSignRenderState(
                    model,
                    SignEditScreen.MAGIC_SCALE_NUMBER * scale,
                    woodType,
                    x1,
                    y,
                    x2,
                    y2
            );
        }

        matrices.popMatrix();

        Vector3f textScale = isHanging ? HangingSignEditScreen.TEXT_SCALE : SignEditScreen.TEXT_SCALE;
        matrices.scale(textScale.x(), textScale.y());

        SignText text = sign.getText(front);
        Component[] signText = text.getMessages(false);

        boolean glow = text.hasGlowingText();
        int outlineColor = AbstractSignRenderer.getDarkColor(text);
        int color = glow ? text.getColor().getTextColor() : outlineColor;

        ctx.nextStratum();
        for (int cur = 0; cur < signText.length; cur++) {
            FormattedCharSequence orderedText = signText[cur].getVisualOrderText();
            if (glow) {
                ctx.guiRenderState.submitText(new TextOutlineGuiElementRenderState(
                        MC.font,
                        orderedText,
                        new Matrix3x2f(ctx.pose()),
                        -MC.font.width(orderedText) / 2,
                        cur * sign.getTextLineHeight() + 5,
                        color,
                        outlineColor,
                        0,
                        false,
                        ctx.scissorStack.peek()
                ));
            } else {
                ctx.drawString(
                        MC.font,
                        orderedText,
                        -MC.font.width(orderedText) / 2,
                        cur * sign.getTextLineHeight() + 5,
                        color,
                        false
                );
            }
        }

        matrices.popMatrix();
    }

    public int getVanillaX(int x) {
        return x / MC.options.guiScale().get();
    }

    public int getVanillaY(int y) {
        return y / MC.options.guiScale().get();
    }

    /**
     * Extends the traditional text element to work with outlines (like signs).
     * @see GuiTextRenderState
     */
    public static class TextOutlineGuiElementRenderState extends GuiTextRenderState {
        public final int outlineColor;

        public TextOutlineGuiElementRenderState(
            Font textRenderer,
            FormattedCharSequence orderedText,
            Matrix3x2f matrix,
            int x,
            int y,
            int color,
            int outlineColor,
            int backgroundColor,
            boolean shadow,
            @Nullable ScreenRectangle clipBounds
        ) {
            super(textRenderer, orderedText, matrix, x, y, color, backgroundColor, shadow, clipBounds);
            this.outlineColor = outlineColor;
        }

        public Font.PreparedText ensuredPrepared() {
            if (this.preparedText == null) {
                // prime mojslop adapted from TextRenderer.drawWithOutline
                Font.PreparedTextBuilder outline = this.font.new PreparedTextBuilder(0.0f, 0.0f, this.outlineColor, false);
                for (int i = -1; i <= 1; ++i) {
                    for (int j = -1; j <= 1; ++j) {
                        if (i == 0 && j == 0) continue;
                        float[] fs = new float[]{x};
                        int k = i;
                        int l = j;
                        this.text.accept((index, style, codePoint) -> {
                            boolean bl = style.isBold();
                            FontSet fontStorage = this.font.getFontSet(style.getFont());
                            GlyphInfo glyph = fontStorage.getGlyphInfo(codePoint, this.font.filterFishyGlyphs);
                            outline.x = fs[0] + (float) k * glyph.getBoldOffset();
                            outline.y = y + (float) l * glyph.getBoldOffset();
                            fs[0] = fs[0] + glyph.getAdvance(bl);
                            return outline.accept(index, style.withColor(this.outlineColor), codePoint);
                        });
                    }
                }

                Font.PreparedText text = this.font.prepareText(
                    this.text,
                    this.x,
                    this.y,
                    this.color,
                    false,
                    0
                );

                this.preparedText = new OutlinedTextPreparation(outline, text);

                ScreenRectangle screenRect = this.preparedText.bounds();
                if (screenRect != null) {
                    screenRect = screenRect.transformMaxBounds(this.pose);
                    this.bounds = this.bounds() != null ? this.bounds().intersection(screenRect) : screenRect;
                }
            }

            return this.preparedText;
        }

        @Override
        @Nullable
        public ScreenRectangle bounds() {
            this.ensuredPrepared();
            return this.bounds;
        }

        private record OutlinedTextPreparation(Font.PreparedText outline, Font.PreparedText text) implements Font.PreparedText {
                @Override
                public void visit(Font.GlyphVisitor glyphDrawer) {
                    this.outline.visit(glyphDrawer);
                    this.text.visit(glyphDrawer);
                }

                @Override
                public @Nullable ScreenRectangle bounds() {
                    return this.outline.bounds();
                }
            }
    }
}
