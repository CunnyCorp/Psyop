/*
 * Modern dark minty toast component for Minecraft
 */
package monster.psyop.client.utility;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LibertyToast implements Toast {
    private static final int BACKGROUND_COLOR = 0xFF1E2F2F;
    private static final int BORDER_COLOR = 0xFF4DB6AC;
    private static final int TITLE_COLOR = 0xFFE0F2F1;
    private static final int TEXT_COLOR = 0xFFB2DFDB;
    private static final int ACCENT_COLOR = 0xFF80CBC4;
    private static final int BORDER_WIDTH = 2;
    private static final int CORNER_RADIUS = 2;

    private final LibertyToastId id;
    private final Component title;
    private final List<FormattedCharSequence> messageLines;
    private long lastChanged;
    private boolean changed;
    private final int width;
    private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

    public LibertyToast(LibertyToastId libertyToastId, Component component, @Nullable Component component2) {
        this(libertyToastId, component, LibertyToast.nullToEmpty(component2),
                Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(component),
                        component2 == null ? 0 : Minecraft.getInstance().font.width(component2))));
    }

    public static LibertyToast multiline(Minecraft minecraft, LibertyToastId libertyToastId, Component component, Component component2) {
        Font font = minecraft.font;
        List<FormattedCharSequence> list = font.split(component2, 200);
        int i = Math.max(200, list.stream().mapToInt(font::width).max().orElse(200));
        return new LibertyToast(libertyToastId, component, list, i + 30);
    }

    private LibertyToast(LibertyToastId libertyToastId, Component component, List<FormattedCharSequence> list, int i) {
        this.id = libertyToastId;
        this.title = component;
        this.messageLines = list;
        this.width = i;
    }

    private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component component) {
        return component == null ? ImmutableList.of() : ImmutableList.of(component.getVisualOrderText());
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return 20 + Math.max(this.messageLines.size(), 1) * 12;
    }

    @Override
    public Toast.@NotNull Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(@NotNull ToastManager toastManager, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }
        double d = (double)this.id.displayTime * toastManager.getNotificationDisplayTimeMultiplier();
        long m = l - this.lastChanged;
        this.wantedVisibility = (double)m < d ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, long l) {
        int height = this.height();

        // Calculate pulse effect (0.0 to 1.0) based on time
        float pulse = (float) (0.5 + 0.5 * Math.sin(l / 200.0));
        int pulsedBorderColor = blendColors(pulse);

        // Draw rounded background
        drawRoundedRect(guiGraphics, this.width, height);

        // Draw pulsed border with rounded corners
        drawRoundedBorder(guiGraphics, this.width, height, pulsedBorderColor);

        // Draw accent line under title
        if (!this.messageLines.isEmpty()) {
            int accentY = 16;
            guiGraphics.fill(10, accentY, this.width - 10, accentY + 1, ACCENT_COLOR);
        }

        // Draw text content
        if (this.messageLines.isEmpty()) {
            guiGraphics.drawString(font, this.title, 18, 12, TITLE_COLOR, false);
        } else {
            guiGraphics.drawString(font, this.title, 18, 7, TITLE_COLOR, false);
            for (int i = 0; i < this.messageLines.size(); ++i) {
                guiGraphics.drawString(font, this.messageLines.get(i), 18, 18 + i * 12, TEXT_COLOR, false);
            }
        }
    }

    // Helper method to blend colors based on pulse value
    private int blendColors(float ratio) {
        if (ratio > 1f) ratio = 1f;
        else if (ratio < 0f) ratio = 0f;

        int r1 = (LibertyToast.BORDER_COLOR >> 16) & 0xFF;
        int g1 = (LibertyToast.BORDER_COLOR >> 8) & 0xFF;
        int b1 = LibertyToast.BORDER_COLOR & 0xFF;

        int r2 = (LibertyToast.ACCENT_COLOR >> 16) & 0xFF;
        int g2 = (LibertyToast.ACCENT_COLOR >> 8) & 0xFF;
        int b2 = LibertyToast.ACCENT_COLOR & 0xFF;

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (r << 16) | (g << 8) | b;
    }

    // Helper method to draw rounded rectangle
    private void drawRoundedRect(GuiGraphics guiGraphics, int width, int height) {
        // Main rectangle body
        guiGraphics.fill(LibertyToast.CORNER_RADIUS, 0, width - LibertyToast.CORNER_RADIUS, height, LibertyToast.BACKGROUND_COLOR);
        guiGraphics.fill(0, LibertyToast.CORNER_RADIUS, width, height - LibertyToast.CORNER_RADIUS, LibertyToast.BACKGROUND_COLOR);

        // Four corner circles
        guiGraphics.fill(0, 0, LibertyToast.CORNER_RADIUS, LibertyToast.CORNER_RADIUS, LibertyToast.BACKGROUND_COLOR);
        guiGraphics.fill(width - LibertyToast.CORNER_RADIUS, 0, width, LibertyToast.CORNER_RADIUS, LibertyToast.BACKGROUND_COLOR);
        guiGraphics.fill(0, height - LibertyToast.CORNER_RADIUS, LibertyToast.CORNER_RADIUS, height, LibertyToast.BACKGROUND_COLOR);
        guiGraphics.fill(width - LibertyToast.CORNER_RADIUS, height - LibertyToast.CORNER_RADIUS, width, height, LibertyToast.BACKGROUND_COLOR);
    }

    // Helper method to draw rounded border
    private void drawRoundedBorder(GuiGraphics guiGraphics, int width, int height, int color) {
        // Top border
        guiGraphics.fill(LibertyToast.CORNER_RADIUS, 0, width - LibertyToast.CORNER_RADIUS, LibertyToast.BORDER_WIDTH, color);
        // Bottom border
        guiGraphics.fill(LibertyToast.CORNER_RADIUS, height - LibertyToast.BORDER_WIDTH, width - LibertyToast.CORNER_RADIUS, height, color);
        // Left border
        guiGraphics.fill(0, LibertyToast.CORNER_RADIUS, LibertyToast.BORDER_WIDTH, height - LibertyToast.CORNER_RADIUS, color);
        // Right border
        guiGraphics.fill(width - LibertyToast.BORDER_WIDTH, LibertyToast.CORNER_RADIUS, width, height - LibertyToast.CORNER_RADIUS, color);

        // Four corner circles for border
        guiGraphics.fill(0, 0, LibertyToast.CORNER_RADIUS, LibertyToast.CORNER_RADIUS, color);
        guiGraphics.fill(width - LibertyToast.CORNER_RADIUS, 0, width, LibertyToast.CORNER_RADIUS, color);
        guiGraphics.fill(0, height - LibertyToast.CORNER_RADIUS, LibertyToast.CORNER_RADIUS, height, color);
        guiGraphics.fill(width - LibertyToast.CORNER_RADIUS, height - LibertyToast.CORNER_RADIUS, width, height, color);
    }

    @Override
    public @NotNull LibertyToastId getToken() {
        return this.id;
    }

    public static void add(ToastManager toastManager, LibertyToastId libertyToastId, Component component, @Nullable Component component2) {
        toastManager.addToast(new LibertyToast(libertyToastId, component, component2));
    }


    @Environment(value = EnvType.CLIENT)
        public record LibertyToastId(long displayTime) {
            public LibertyToastId() {
                this(2000L);
            }
        }
}