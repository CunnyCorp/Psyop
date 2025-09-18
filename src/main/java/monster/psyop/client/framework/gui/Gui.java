package monster.psyop.client.framework.gui;

import imgui.*;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.type.ImBoolean;
import monster.psyop.client.Liberty;
import monster.psyop.client.config.Config;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.gui.hud.HudHandler;
import monster.psyop.client.framework.gui.themes.OfficialTheme;
import monster.psyop.client.framework.gui.themes.ThemeManager;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.framework.gui.views.ViewHandler;
import monster.psyop.client.impl.events.game.OnKeyInput;
import monster.psyop.client.utility.PathIndex;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static monster.psyop.client.Liberty.MC;

public class Gui extends RenderProxy {
    public static ImBoolean IS_LOADED = new ImBoolean(false);
    public static ImBoolean IS_STYLING = new ImBoolean(false);
    public static OfficialTheme THEME = new OfficialTheme();

    public static ImGuiIO io;

    public void launch() {
        Liberty.EVENT_HANDLER.add(this);
        initialize();
        preRun();
    }

    @EventListener(inGame = false)
    public void onKey(OnKeyInput event) {
        if (event.action != 1) {
            return;
        }

        if (event.key == Config.get().coreSettings.guiBind.get()) {
            IS_LOADED.set(!IS_LOADED.get());
            if (Config.get() != null) {
                Config.get().save();
            }
        }

        if (event.key == KeyUtils.ESCAPE) {
            if (IS_LOADED.get()) {
                IS_LOADED.set(false);
                if (Config.get() != null) {
                    Config.get().save();
                }
            }
        }
    }

    private void initialize() {
        init();

        ThemeManager.init();

        io = ImGui.getIO();

        io.setIniFilename("imgui_config");
        io.setKeyMap(KeyUtils.keyMap);
        io.setNavInputs(0, 0);
        io.setNavActive(false);
        io.setConfigFlags(ImGuiConfigFlags.NavNoCaptureKeyboard | ImGuiConfigFlags.IsSRGB);

        THEME.load(ImGui.getStyle());

        if (Files.exists(PathIndex.CLIENT.resolve("themes").resolve("theme.txt"))) {
            ThemeManager.loadTheme(PathIndex.CLIENT.resolve("themes").resolve("theme.txt"));
        }
    }


    @Override
    public void process() {
        if (IS_LOADED.get()) {

            if (MC.mouseHandler.isMouseGrabbed()) MC.mouseHandler.releaseMouse();

            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("Views")) {

                    for (View view : ViewHandler.getViews()) {
                        ImGui.menuItem(view.displayName(), "", ViewHandler.state(view.getClass()));
                        if (ImGui.isMouseClicked(2) && ImGui.isItemHovered()) {
                            if (Config.get().guiSettings.consistentViews.contains(view.name())) {
                                Config.get().guiSettings.consistentViews.remove(view.name());
                                Liberty.log("Removed view {} from consistency.", view.name());
                            } else {
                                Config.get().guiSettings.consistentViews.add(view.name());
                                Liberty.log("Added view {} to be consistent.", view.name());
                            }
                        }
                    }

                    ImGui.menuItem("Styling", "", IS_STYLING);
                    ImGui.endMenu();
                }

                if (ImGui.button("Close")) {
                    IS_LOADED.set(false);
                }

                ImGui.endMainMenuBar();
            }

            if (Config.get() == null) return;

            ViewHandler.showAll();

            if (IS_STYLING.get()) {
                ImGui.showStyleEditor(ImGui.getStyle());
            }
        } else {
            if (Config.get() == null) return;

            for (View view : ViewHandler.getViews()) {
                if (Config.get().guiSettings.consistentViews.contains(view.name())) {
                    view.show();
                }
            }
        }

        HudHandler.showAll();

        if (!MC.mouseHandler.isMouseGrabbed() && MC.screen == null) MC.mouseHandler.grabMouse();
    }

    protected void preRun() {
        Liberty.LOG.info("Starting cunny GUI");
    }
}