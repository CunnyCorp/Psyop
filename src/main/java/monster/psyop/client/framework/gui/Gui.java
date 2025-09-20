package monster.psyop.client.framework.gui;

import imgui.*;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.type.ImBoolean;
import monster.psyop.client.Psyop;
import monster.psyop.client.config.Config;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.gui.hud.HudHandler;
import monster.psyop.client.framework.gui.themes.OfficialTheme;
import monster.psyop.client.framework.gui.themes.ThemeManager;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.framework.gui.views.ViewHandler;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.game.OnKeyInput;
import monster.psyop.client.impl.events.game.OnMouseClick;
import monster.psyop.client.utility.PathIndex;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static monster.psyop.client.Psyop.MC;

public class Gui extends RenderProxy {
    public static ImBoolean IS_LOADED = new ImBoolean(false);
    public static ImBoolean IS_STYLING = new ImBoolean(false);
    public static OfficialTheme THEME = new OfficialTheme();

    public static ImGuiIO io;

    private int WATERMARK_ID = 0;
    private static final float WATERMARK_SCALE = 0.18f;
    private static int WATERMARK_ORIGINAL_WIDTH = 0;
    private static int WATERMARK_ORIGINAL_HEIGHT = 0;
    private static long lastGlorpUpdate = 0;
    private static final List<Glorp> GLORPS = new ArrayList<>();
    private static boolean isGlorp = false;

    public void launch() {
        Psyop.EVENT_HANDLER.add(this);
        initialize();
        preRun();
    }

    @EventListener(inGame = false)
    public void onClick(OnMouseClick event) {
        if (event.action != 1) {
            return;
        }

        for (Glorp glorp : GLORPS) {
            //glorp.handleClicked(event.key);
        }
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

        int glorp = loadGlorp();
        if (glorp != -1) {
            isGlorp = true;
        } else {
            System.err.println("Put Glorp back...");
            System.exit(1);
        }

        WATERMARK_ID = loadTexture("/watermark.png");
    }


    @Override
    public void process() {
        ImDrawList drawList = ImGui.getBackgroundDrawList();

        if (WATERMARK_ID != -1 && WATERMARK_ORIGINAL_WIDTH > 0 && WATERMARK_ORIGINAL_HEIGHT > 0) {
            float scaledWidth = WATERMARK_ORIGINAL_WIDTH * WATERMARK_SCALE;
            float scaledHeight = WATERMARK_ORIGINAL_HEIGHT * WATERMARK_SCALE;
            float padding = 10f;
            float halfPad = padding / 2;

            drawBackground(halfPad, halfPad, padding + halfPad + scaledWidth, padding + halfPad + scaledHeight);
            drawList.addImage(
                    WATERMARK_ID,
                    padding, padding,
                    padding + scaledWidth, padding + scaledHeight,
                    0, 0, 1, 1
            );
        }

        float screenWidth = io.getDisplaySizeX();
        float screenHeight = io.getDisplaySizeY();

        String position = "0, 0, 0 - (0, 0)";

        if (MC.player != null) {
            position = Math.round(MC.player.getX()) + ", " + Math.round(MC.player.getY()) + ", " + Math.round(MC.player.getZ()) + " - (" + Math.round(MC.player.getX() / 8) + ", " +Math.round(MC.player.getZ() / 8) + ")";
        }

        float textX = 20f;
        float textY = screenHeight - 30f;

        drawString(position, textX, textY, 240f, true);

        if (IS_LOADED.get()) {
            if (isGlorp && !GLORPS.isEmpty()) {
                long deltaTime = System.currentTimeMillis() - lastGlorpUpdate;

                for (Glorp glorp : GLORPS) {
                    // About 60 fps
                    if (deltaTime > 17) {
                        glorp.update((int) io.getDisplaySizeX(), (int) io.getDisplaySizeY());
                        lastGlorpUpdate = System.currentTimeMillis();
                    }

                    glorp.render();
                }
            }

            if (MC.mouseHandler.isMouseGrabbed()) MC.mouseHandler.releaseMouse();

            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("Views")) {

                    for (View view : ViewHandler.getViews()) {
                        ImGui.menuItem(view.displayName(), "", ViewHandler.state(view.getClass()));
                        if (ImGui.isMouseClicked(2) && ImGui.isItemHovered()) {
                            if (Config.get().guiSettings.consistentViews.contains(view.name())) {
                                Config.get().guiSettings.consistentViews.remove(view.name());
                                Psyop.log("Removed view {} from consistency.", view.name());
                            } else {
                                Config.get().guiSettings.consistentViews.add(view.name());
                                Psyop.log("Added view {} to be consistent.", view.name());
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

    public void drawBackground(float x, float y, float width, float height) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        drawList.addRectFilled(x, y, width, height, new ImColorW(new Color(71, 69, 69, 182)).packed(), 4f, ImDrawFlags.RoundCornersAll);
        drawList.addRect(x, y, width, height, new ImColorW(new Color(25, 24, 24, 255)).packed(), 4f, ImDrawFlags.RoundCornersAll);
    }

    public void drawString(String text, float x, float y, float minWidth, boolean withBackground) {
        ImDrawList drawList = ImGui.getBackgroundDrawList();

        if (withBackground) {
            ImVec2 textSize = new ImVec2();
            ImGui.calcTextSize(textSize, text);

            float padding = 10f;
            float bgX = x - padding;
            float bgY = y - padding;
            float bgWidth = Math.max(minWidth, textSize.x + padding * 2);
            float bgHeight = textSize.y + padding * 2;

            // Draw background first
            drawBackground(bgX, bgY, bgX + bgWidth, bgY + bgHeight);
        }

        // Draw the text
        drawList.addText(x, y, ImGui.getColorU32(ImGuiCol.Text), text);
    }


    protected void preRun() {
        Psyop.LOG.info("Starting GUI");
    }

    private int loadTexture(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                Psyop.LOG.error("Image not found in resources");
                return -1;
            }

            BufferedImage image = ImageIO.read(is);
            int width = image.getWidth();
            int height = image.getHeight();

            if (path.equals("/watermark.png")) {
                WATERMARK_ORIGINAL_WIDTH = width;
                WATERMARK_ORIGINAL_HEIGHT = height;
            }

            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);

            ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();

            int textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            return textureId;
        } catch (IOException e) {
            Psyop.LOG.error("Failed to load texture", e);
            return -1;
        }
    }

    private int loadGlorp() {
        try {
            InputStream is = getClass().getResourceAsStream("/glorp.png");
            if (is == null) {
                Psyop.LOG.error("Glorp not found.");
                return -1;
            }

            BufferedImage image = ImageIO.read(is);
            int width = image.getWidth();
            int height = image.getHeight();

            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);

            ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();

            int textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            for (int i = 0; i < 24; i++) {
                GLORPS.add(new Glorp(textureId, width, height));
            }

            return textureId;
        } catch (IOException e) {
            Psyop.LOG.error("Failed to load DVD logo texture", e);
            return -1;
        }
    }

    public static class Glorp {
        private int x, y;
        private int width, height;
        private final int originalWidth;
        private final int originalHeight;
        private int velocityX = Psyop.RANDOM.nextInt(1, 4), velocityY = Psyop.RANDOM.nextInt(1, 4);
        private final int textureId;
        private float scale = 0.25f;
        private float rotation = 0f;
        private float rotationSpeed = Psyop.RANDOM.nextFloat() * 2f + 1f;

        public Glorp(int textureId, int originalWidth, int originalHeight) {
            this.textureId = textureId;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
            updateSize();
            this.x = Psyop.RANDOM.nextInt(20, MC.getWindow().getWidth() - 20);
            this.y = Psyop.RANDOM.nextInt(20, MC.getWindow().getHeight() - 20);

            if (Psyop.RANDOM.nextBoolean()) {
                this.velocityX *= -1;
            }

            if (Psyop.RANDOM.nextBoolean()) {
                this.velocityY *= -1;
            }

            this.rotation = Psyop.RANDOM.nextFloat() * 360f;

            if (Psyop.RANDOM.nextBoolean()) {
                this.rotationSpeed *= -1;
            }
        }

        public boolean isMouseOver() {
            if (ImGui.isMouseHoveringRect(x, y, width, height)) {
                return true;
            }

            return false;
        }

        public void handleClicked(int key) {
            if (isMouseOver()) {
                if (key == 0) {
                    this.scale = Math.min(1.0f, this.scale + (this.scale * 0.15f));
                } else if (key == 1) {
                    this.scale = Math.max(0.075f, this.scale - (this.scale * 0.25f));
                }
            }


        }

        private void updateSize() {
            this.width = (int)(originalWidth * scale);
            this.height = (int)(originalHeight * scale);
        }

        public void update(int screenWidth, int screenHeight) {
            x += velocityX;
            y += velocityY;

            rotation += rotationSpeed;
            if (rotation > 360f) rotation -= 360f;
            if (rotation < 0f) rotation += 360f;

            boolean widthChanged = false;
            boolean heightChanged = false;

            if (x <= 20 || x + width >= screenWidth - 20) {
                velocityX *= -1;
                widthChanged = true;
            }

            if (y <= 20 || y + height >= screenHeight - 20) {
                velocityY *= -1;
                heightChanged = true;
            }

            if ((widthChanged || heightChanged) && !(widthChanged && heightChanged) && Psyop.RANDOM.nextBoolean()) {
                if (widthChanged) {
                    velocityY *= -1;
                } else {
                    velocityX *= -1;
                }
            }
        }

        public void render() {
            ImDrawList drawList = ImGui.getBackgroundDrawList();

            float centerX = x + width / 2f;
            float centerY = y + height / 2f;

            drawList.addImageQuad(
                    textureId,
                    centerX + (float) Math.cos(Math.toRadians(rotation)) * (x - centerX) - (float) Math.sin(Math.toRadians(rotation)) * (y - centerY),
                    centerY + (float) Math.sin(Math.toRadians(rotation)) * (x - centerX) + (float) Math.cos(Math.toRadians(rotation)) * (y - centerY),

                    centerX + (float) Math.cos(Math.toRadians(rotation)) * (x + width - centerX) - (float) Math.sin(Math.toRadians(rotation)) * (y - centerY),
                    centerY + (float) Math.sin(Math.toRadians(rotation)) * (x + width - centerX) + (float) Math.cos(Math.toRadians(rotation)) * (y - centerY),

                    centerX + (float) Math.cos(Math.toRadians(rotation)) * (x + width - centerX) - (float) Math.sin(Math.toRadians(rotation)) * (y + height - centerY),
                    centerY + (float) Math.sin(Math.toRadians(rotation)) * (x + width - centerX) + (float) Math.cos(Math.toRadians(rotation)) * (y + height - centerY),

                    centerX + (float) Math.cos(Math.toRadians(rotation)) * (x - centerX) - (float) Math.sin(Math.toRadians(rotation)) * (y + height - centerY),
                    centerY + (float) Math.sin(Math.toRadians(rotation)) * (x - centerX) + (float) Math.cos(Math.toRadians(rotation)) * (y + height - centerY)
            );
        }
    }
}