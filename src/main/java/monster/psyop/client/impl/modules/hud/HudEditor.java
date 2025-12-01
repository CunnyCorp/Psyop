package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.type.ImInt;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.gui.utility.DrawUtils;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.framework.rendering.hud.HudHandler;
import monster.psyop.client.impl.events.OnGuiRender;
import monster.psyop.client.impl.events.game.OnMouseClick;

import java.awt.*;

public class HudEditor extends Module {
    public static HUD focusedHud = null;
    public static int color = new ImColorW(new Color(255, 255, 255, 100)).packed();
    private int dragOffsetX, dragOffsetY;

    private static final ImColorW[] glowColors = {
            new ImColorW(new Color(0, 255, 255, 50)),
            new ImColorW(new Color(0, 255, 255, 100)),
            new ImColorW(new Color(0, 255, 255, 150)),
            new ImColorW(new Color(0, 255, 255, 200))
    };


    public HudEditor() {
        super(Categories.HUD, "hud-editor", "Lets you edit HUD elements.");
    }

    @EventListener(inGame = false, priority = 1)
    public void onRender2D(OnGuiRender event) {
        drawSelectionOutlines();

        if (focusedHud == null) {
            return;
        }

        ImVec2 vec2 = ImGui.getMousePos();
        int mouseX = (int) vec2.x;
        int mouseY = (int) vec2.y;

        focusedHud.xPos.value(new ImInt(mouseX - dragOffsetX));
        focusedHud.yPos.value(new ImInt(mouseY - dragOffsetY));

    }

    @EventListener(inGame = false, priority = 1)
    public void onMouseClick(OnMouseClick event) {
        if (event.key == 0 && event.action == 1) {
            HUD hoveredHud = HudHandler.getHoveredHud();
            if (hoveredHud != null) {
                focusedHud = hoveredHud;

                ImVec2 mousePos = ImGui.getMousePos();
                dragOffsetX = (int) mousePos.x - HudHandler.getRenderX(focusedHud);
                dragOffsetY = (int) mousePos.y - focusedHud.yPos.get();
            }
        }

        if (focusedHud != null) {
            if (!focusedHud.active() || focusedHud.getWidth() == 0 || focusedHud.getHeight() == 0) {
                focusedHud = null;
                return;
            }

            if (event.key == 1 && event.action == 0) {
                focusedHud = null;
            }
        }
    }

    private void drawSelectionOutlines() {
        if (focusedHud == null) return;

        int x = focusedHud.xPos.get();
        int y = focusedHud.yPos.get();
        int width = focusedHud.getWidth();
        int height = focusedHud.getHeight();

        drawGlowingOutline(x, y, width, height);
    }

    private void drawGlowingOutline(int x, int y, int width, int height) {
        int[] outlineOffsets = {4, 3, 2, 1};

        for (int i = 0; i < glowColors.length; i++) {
            int offset = outlineOffsets[i];
            drawOutline(x - offset, y - offset, width + offset * 2, height + offset * 2, glowColors[i]);
        }
    }

    private void drawOutline(int x, int y, int width, int height, ImColorW color) {
        DrawUtils.drawRect(x, y, width, height, color, 0.0f, 1.5f);
    }
}
