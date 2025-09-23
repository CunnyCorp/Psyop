// NotificationHUD.java
package monster.psyop.client.impl.modules.hud;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImDrawFlags;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.gui.GradientUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;

import java.awt.*;

public class NotificationHUD extends HUD {
    public ColorSetting backgroundColor =
            new ColorSetting.Builder()
                    .name("background-color")
                    .defaultTo(new float[]{0.10f, 0.10f, 0.12f, 0.95f})
                    .addTo(coreGroup);
    public FloatSetting animationSpeed =
            new FloatSetting.Builder()
                    .name("animation-speed")
                    .range(0.1f, 2.0f)
                    .defaultTo(0.8f)
                    .addTo(coreGroup);
    public FloatSetting cornerRadius =
            new FloatSetting.Builder()
                    .name("corner-radius")
                    .range(0.0f, 12.0f)
                    .defaultTo(6.0f)
                    .addTo(coreGroup);

    private final GradientUtils gradientUtils = new GradientUtils(0.5f);
    private final NotificationManager notificationManager = NotificationManager.get();

    private float globalAnimationPhase = 0f;
    private long lastAnimationUpdate = System.currentTimeMillis();

    public NotificationHUD() {
        super("notifications", "Displays notifications.");
        if (!active()) active(true);
    }

    @EventListener(inGame = false)
    public void onTick(OnTick.Pre event) {
        gradientUtils.updateAnimation();
        notificationManager.update();
        updateAnimations();
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastAnimationUpdate) / 1000.0f;
        globalAnimationPhase = (globalAnimationPhase + delta * animationSpeed.get()) % 1.0f;
        lastAnimationUpdate = currentTime;
    }

    @EventListener(inGame = false)
    public void render(On2DRender event) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        float screenWidth = ImGui.getMainViewport().getSizeX();
        float margin = 10.0f;
        float startX = screenWidth - 300.0f - margin;

        int notificationIndex = 0;

        for (NotificationEvent notification : notificationManager.getNotifications()) {
            float animationProgress = calculateAnimationProgress(notification);
            if (animationProgress <= 0.01f) continue;

            renderNotification(drawList, notification, startX, margin + (notificationIndex * 60),
                    animationProgress, notificationIndex);
            notificationIndex++;
        }
    }

    private float calculateAnimationProgress(NotificationEvent notification) {
        long currentTime = System.currentTimeMillis();
        float progress = notification.getProgress(currentTime);

        if (progress < 0.1f) {
            return progress / 0.1f;
        } else if (progress > 0.9f) {
            return 1.0f - ((progress - 0.9f) / 0.1f);
        } else {
            return 1.0f;
        }
    }

    private void renderNotification(ImDrawList drawList, NotificationEvent notification, float x, float y, float alpha, int index) {
        float width = 300.0f;
        float padding = 12.0f;
        float accentWidth = 4.0f;

        ImVec2 titleSize = new ImVec2();
        ImVec2 messageSize = new ImVec2();

        String title = notification.title.isEmpty() ? notification.type.name() : notification.title;
        ImGui.calcTextSize(titleSize, title);
        ImGui.calcTextSize(messageSize, notification.message);

        float height = Math.max(50.0f, titleSize.y + messageSize.y + padding * 2);

        float animOffsetX = (1.0f - alpha) * 50.0f;
        float currentX = x + animOffsetX;

        Color bgColor = applyAlpha(GradientUtils.getColorFromSetting(backgroundColor), alpha);
        Color accent = applyAlpha(new Color(
                notification.type.color[0],
                notification.type.color[1],
                notification.type.color[2],
                notification.type.color[3]
        ), alpha);

        drawList.addRectFilled(currentX, y, currentX + width, y + height,
                new ImColorW(bgColor).packed(), cornerRadius.get(), ImDrawFlags.RoundCornersAll);

        drawList.addRectFilled(currentX, y, currentX + accentWidth, y + height,
                new ImColorW(accent).packed(), cornerRadius.get(), ImDrawFlags.RoundCornersLeft);

        float progress = notification.getProgress(System.currentTimeMillis());
        float progressWidth = (width - accentWidth) * progress;
        Color progressColor = applyAlpha(new Color(
                accent.getRed(), accent.getGreen(), accent.getBlue(), 80
        ), alpha * 0.5f);

        drawList.addRectFilled(currentX + accentWidth, y + height - 2,
                currentX + accentWidth + progressWidth, y + height,
                new ImColorW(progressColor).packed(), 0, ImDrawFlags.None);

        Color textColor = applyAlpha(Color.WHITE, alpha);
        float textX = currentX + accentWidth + padding;
        float textY = y + padding;

        if (!notification.title.isEmpty()) {
            drawList.addText(textX, textY, new ImColorW(textColor).packed(), title);
            textY += titleSize.y + 4;
        }

        drawList.addText(textX, textY, new ImColorW(textColor).packed(), notification.message);

        if (alpha > 0.9f) {
            drawAnimatedAccent(drawList, currentX, y, accentWidth, height, accent, index);
        }
    }

    private void drawAnimatedAccent(ImDrawList drawList, float x, float y, float width, float height,
                                    Color baseColor, int index) {
        float wavePhase = (globalAnimationPhase + index * 0.2f) % 1.0f;
        int segments = 8;
        float segmentHeight = height / segments;

        for (int i = 0; i < segments; i++) {
            float segmentY = y + i * segmentHeight;
            float waveFactor = (wavePhase + (float)i / segments) % 1.0f;

            float brightness = 0.8f + 0.2f * (float)Math.sin(waveFactor * Math.PI * 2);

            Color waveColor = new Color(
                    Math.min(255, (int)(baseColor.getRed() * brightness)),
                    Math.min(255, (int)(baseColor.getGreen() * brightness)),
                    Math.min(255, (int)(baseColor.getBlue() * brightness)),
                    baseColor.getAlpha()
            );

            drawList.addRectFilled(x, segmentY, x + width, segmentY + segmentHeight,
                    new ImColorW(waveColor).packed(), 0, ImDrawFlags.None);
        }
    }

    private Color applyAlpha(Color color, float alpha) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int)(color.getAlpha() * alpha)
        );
    }
}