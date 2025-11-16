package monster.psyop.client.framework.gui.views.features;

import com.google.gson.annotations.SerializedName;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImString;
import monster.psyop.client.Psyop;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.BookSettings;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.StringUtils;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static monster.psyop.client.Psyop.MC;
import static monster.psyop.client.Psyop.RANDOM;

public class BookEditorView extends View {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    public static final String[] INVISIBLE_CHARS = {" ", "\u007f", "§"};
    private final List<String> PAGES_CACHE = new ArrayList<>();
    public BookSettings config;
    private String title = "";
    private final Runnable autoSignTask =
            () -> {
                if (config.autoSign.get() && MC.player != null) signBook();
            };

    private final ImString statusMessage = new ImString("");
    private long statusMessageTime = 0;
    private static final long STATUS_MESSAGE_DURATION = 3000; // 3 seconds

    @Override
    public String name() {
        return "book-editor";
    }

    @Override
    public void show() {
        long currentTime = System.currentTimeMillis();

        PersistentGuiSettings settings = Config.get().booksGui;

        if (ImGui.begin(displayName(), state())) {
            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(550, 750, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(0, 0, ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 6.0f, 4.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 3.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 3.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12.0f, 12.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 6.0f, 4.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.CellPadding, 4.0f, 2.0f);

            ImGui.getFont().setScale(1.2f);
            ImGui.textColored(0.00f, 0.75f, 0.75f, 1.0f, "Book Editor");
            ImGui.getFont().setScale(1.0f);

            ImGui.sameLine(ImGui.getWindowWidth() - 120);
            ImGui.textDisabled("v1.0.0");

            ImGui.separator();
            ImGui.spacing();

            if (currentTime - statusMessageTime < STATUS_MESSAGE_DURATION) {
                float fade = 1.0f - ((currentTime - statusMessageTime) / (float) STATUS_MESSAGE_DURATION);
                ImGui.textColored(0.85f, 0.35f, 0.75f, fade, statusMessage.get());
            }

            if (ImGui.collapsingHeader("Title", ImGuiTreeNodeFlags.DefaultOpen)) {
                ImGui.textWrapped("Configure your book's title.");
                ImGui.spacing();

                if (config.title.modifier != BookModifier.NONE) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.85f, 0.35f, 0.75f, 1.0f);
                    ImGui.textWrapped("Active Modification: " + StringUtils.readable(config.title.modifier.name()));
                    ImGui.popStyleColor();
                }

                ImGui.pushItemWidth(-1);
                ImGui.inputTextWithHint("##title_text", "Enter title text...", config.title.text,
                        ImGuiInputTextFlags.CallbackResize);
                ImGui.popItemWidth();

                ImGui.spacing();
                ImGui.text("Title Length:");
                ImGui.sameLine();
                ImGui.setNextItemWidth(ImGui.getWindowWidth() - 150);
                ImGui.sliderInt("##title_length", config.title.length.getData(), 1, 128, "%d characters");

                ImGui.spacing();
                ImGui.checkbox("Use Unicode Glyphs", config.title.unicode);

                ImGui.spacing();
                ImGui.beginGroup();
                if (ImGui.button("Set Title", 120, 30)) {
                    config.title.modifier = BookModifier.NONE;
                    title = config.title.text.get();
                    addStatusMessage("Title set: '" + title + "'");
                }
                ImGui.sameLine();
                if (ImGui.button("Invisible Title", 120, 30)) {
                    config.title.modifier = BookModifier.INVISIBLE;
                    StringBuilder tempTitle = new StringBuilder();
                    for (int i = 0; i < config.title.length.get(); i++) {
                        tempTitle.append(INVISIBLE_CHARS[RANDOM.nextInt(0, INVISIBLE_CHARS.length)]);
                    }
                    title = tempTitle.toString();
                    addStatusMessage("Created invisible title of " + config.title.length.get() + " characters");
                }
                ImGui.sameLine();
                if (ImGui.button("Random Title", 120, 30)) {
                    config.title.modifier =
                            config.title.unicode.get() ? BookModifier.RANDOMIZED_UNICODE : BookModifier.RANDOMIZED;
                    title = StringUtils.randomText(config.title.length.get(), config.title.unicode.get());
                    addStatusMessage("Generated random title");
                }
                ImGui.endGroup();
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();

            if (ImGui.collapsingHeader("Pages", ImGuiTreeNodeFlags.DefaultOpen)) {
                ImGui.textWrapped("Configure your book's pages.");
                ImGui.spacing();

                if (config.pages.modifier != BookModifier.NONE) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.85f, 0.35f, 0.75f, 1.0f);
                    ImGui.textWrapped("Active Modification: " + StringUtils.readable(config.pages.modifier.name()));
                    ImGui.popStyleColor();
                }

                ImGui.columns(2, "page_settings", false);
                ImGui.setColumnWidth(0, 120);

                ImGui.text("Page Count:");
                ImGui.nextColumn();
                ImGui.sliderInt("##page_count", config.pages.count.getData(), 1, 100, "%d pages");
                ImGui.nextColumn();

                ImGui.text("Page Length:");
                ImGui.nextColumn();
                ImGui.sliderInt("##page_length", config.pages.length.getData(), 1, 1024, "%d characters");
                ImGui.nextColumn();

                ImGui.columns(1);

                ImGui.spacing();
                ImGui.pushItemWidth(-1);
                ImGui.inputTextMultiline("##page_text", config.pages.text,
                        -1, 100, ImGuiInputTextFlags.CallbackResize);
                ImGui.popItemWidth();

                ImGui.spacing();
                ImGui.checkbox("Use Unicode Glyphs##pages", config.pages.unicode);

                ImGui.spacing();
                ImGui.beginGroup();
                if (ImGui.button("Fill Pages", 120, 30)) {
                    config.pages.modifier = BookModifier.NONE;
                    PAGES_CACHE.clear();
                    for (int i = 0; i < config.pages.count.get(); i++) {
                        PAGES_CACHE.add(config.pages.text.get());
                    }
                    addStatusMessage("Filled " + config.pages.count.get() + " pages");
                }
                ImGui.sameLine();
                if (ImGui.button("Invisible Pages", 120, 30)) {
                    config.pages.modifier = BookModifier.INVISIBLE;
                    PAGES_CACHE.clear();
                    for (int i = 0; i < config.pages.count.get(); i++) {
                        StringBuilder page = new StringBuilder();
                        for (int y = 0; y < config.pages.length.get(); y++) {
                            page.append(INVISIBLE_CHARS[RANDOM.nextInt(0, INVISIBLE_CHARS.length)]);
                        }
                        PAGES_CACHE.add(page.toString());
                    }
                    addStatusMessage("Created " + config.pages.count.get() + " invisible pages");
                }
                ImGui.sameLine();
                if (ImGui.button("Random Pages", 120, 30)) {
                    config.pages.modifier =
                            config.pages.unicode.get() ? BookModifier.RANDOMIZED_UNICODE : BookModifier.RANDOMIZED;
                    PAGES_CACHE.clear();
                    for (int i = 0; i < config.pages.count.get(); i++) {
                        PAGES_CACHE.add(StringUtils.randomText(config.pages.length.get(), config.pages.unicode.get()));
                    }
                    addStatusMessage("Generated " + config.pages.count.get() + " random pages");
                }
                ImGui.endGroup();
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();

            if (ImGui.collapsingHeader("Actions", ImGuiTreeNodeFlags.DefaultOpen)) {
                ImGui.textWrapped("Finalize and send your book.");
                ImGui.spacing();

                ImGui.beginGroup();
                if (ImGui.button("Sign Book", 130, 35)) {
                    signBook();
                }
                ImGui.sameLine();
                if (ImGui.button("Send Book", 130, 35)) {
                    sendBook();
                }
                ImGui.sameLine();
                if (ImGui.button("Sign Empty", 130, 35)) {
                    signEmptyBook();
                }
                ImGui.sameLine();
                if (ImGui.button("Send Empty", 130, 35)) {
                    writeEmptyBook();
                }
                ImGui.endGroup();

                ImGui.spacing();
                ImGui.separator();
                ImGui.spacing();

                ImGui.text("Auto Sign:");
                ImGui.spacing();

                ImGui.beginGroup();
                ImGui.checkbox("Enable Auto-Sign", config.autoSign);
                ImGui.sameLine();
                ImGui.setNextItemWidth(120);
                ImGui.sliderInt("Delay##sign_delay", config.signDelay.getData(), 1, 120, "%d seconds");
                ImGui.sameLine();
                if (ImGui.button("Start Timer", 120, 0)) {
                    try {
                        executor.shutdown();
                        executor.scheduleAtFixedRate(
                                autoSignTask, config.signDelay.get(), config.signDelay.get(), TimeUnit.SECONDS);
                        addStatusMessage("Auto-sign timer started (" + config.signDelay.get() + "s)");
                    } catch (Exception e) {
                        Psyop.error("Exception in Build Timer: " + e.getLocalizedMessage());
                        addStatusMessage("Error starting timer: " + e.getMessage());
                    }
                }
                ImGui.endGroup();

                if (config.autoSign.get()) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.00f, 0.75f, 0.75f, 1.0f);
                    ImGui.text("Auto-sign enabled");
                    ImGui.popStyleColor();
                }
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.textDisabled("Book Editor v1.0 · " + PAGES_CACHE.size() + " pages cached");

            ImGui.popStyleVar(6);
        }

        settings.x = ImGui.getWindowPosX();
        settings.y = ImGui.getWindowPosY();
        settings.width = ImGui.getWindowWidth();
        settings.height = ImGui.getWindowHeight();
        ImGui.end();
    }

    private void addStatusMessage(String message) {
        statusMessage.set(message);
        statusMessageTime = System.currentTimeMillis();
        Psyop.log("[Book Editor] " + message);
    }

    public void signBook() {
        switch (config.title.modifier) {
            case RANDOMIZED, RANDOMIZED_UNICODE ->
                    title = StringUtils.randomText(config.title.length.get(), config.title.unicode.get());

            case INVISIBLE -> {
                StringBuilder tempTitle = new StringBuilder();
                for (int i = 0; i < config.title.length.get(); i++) {
                    tempTitle.append(INVISIBLE_CHARS[RANDOM.nextInt(0, INVISIBLE_CHARS.length)]);
                }
                title = tempTitle.toString();
            }
        }

        switch (config.pages.modifier) {
            case RANDOMIZED, RANDOMIZED_UNICODE -> {
                PAGES_CACHE.clear();
                for (int i = 0; i < config.pages.count.get(); i++) {
                    PAGES_CACHE.add(StringUtils.randomText(config.pages.length.get(), config.pages.unicode.get()));
                }
            }

            case INVISIBLE -> {
                PAGES_CACHE.clear();
                for (int i = 0; i < config.pages.count.get(); i++) {
                    StringBuilder page = new StringBuilder();
                    for (int y = 0; y < config.pages.length.get(); y++) {
                        page.append(INVISIBLE_CHARS[RANDOM.nextInt(0, INVISIBLE_CHARS.length)]);
                    }
                    PAGES_CACHE.add(page.toString());
                }
            }
        }

        if (MC.player == null) {
            Psyop.warn("Player is null, not in-game.");
            addStatusMessage("Error: Not in-game");
        } else {
            int slot = InventoryUtils.findAnySlot(Items.WRITABLE_BOOK);
            if (slot == -1) {
                Psyop.warn("No books found.");
                addStatusMessage("Error: No writable books found");
                return;
            }

            if (!InventoryUtils.isHotbarSlot(slot) && slot != 44) {
                InventoryUtils.swapToHotbar(slot, MC.player.getInventory().getSelectedSlot());
                assert MC.player != null;
                slot = MC.player.getInventory().getSelectedSlot();
            }

            if (slot == -1) {
                Psyop.warn("Book was not found.");
                addStatusMessage("Error: Book not found");
                return;
            } else if (InventoryUtils.isHotbarSlot(slot)) {
                InventoryUtils.swapSlot(slot > 8 ? slot - InventoryUtils.getHotbarOffset() : slot);
            }

            PacketUtils.send(new ServerboundEditBookPacket(
                    MC.player.getInventory().getSelectedSlot(), PAGES_CACHE, Optional.of(title)));

            addStatusMessage("Book signed successfully");
        }
    }

    public void sendBook() {
        switch (config.pages.modifier) {
            case RANDOMIZED, RANDOMIZED_UNICODE -> {
                PAGES_CACHE.clear();
                for (int i = 0; i < config.pages.count.get(); i++) {
                    PAGES_CACHE.add(StringUtils.randomText(config.pages.length.get(), config.pages.unicode.get()));
                }
            }

            case INVISIBLE -> {
                PAGES_CACHE.clear();
                for (int i = 0; i < config.pages.count.get(); i++) {
                    StringBuilder page = new StringBuilder();
                    for (int y = 0; y < config.pages.length.get(); y++) {
                        page.append(INVISIBLE_CHARS[RANDOM.nextInt(0, INVISIBLE_CHARS.length)]);
                    }
                    PAGES_CACHE.add(page.toString());
                }
            }
        }

        if (MC.player == null) {
            Psyop.warn("Player is null, not in-game.");
            addStatusMessage("Error: Not in-game");
        } else {
            int slot = InventoryUtils.findAnySlot(Items.WRITABLE_BOOK);
            if (slot == -1) {
                Psyop.warn("No books found.");
                addStatusMessage("Error: No writable books found");
                return;
            }

            if (!InventoryUtils.isHotbarSlot(slot) && slot != 44) {
                InventoryUtils.swapToHotbar(slot, MC.player.getInventory().getSelectedSlot());
                assert MC.player != null;
                slot = MC.player.getInventory().getSelectedSlot();
            }

            if (slot == -1) {
                Psyop.warn("Book was not found.");
                addStatusMessage("Error: Book not found");
                return;
            } else if (InventoryUtils.isHotbarSlot(slot)) {
                InventoryUtils.swapSlot(slot > 8 ? slot - InventoryUtils.getHotbarOffset() : slot);
            }

            PacketUtils.send(new ServerboundEditBookPacket(
                    MC.player.getInventory().getSelectedSlot(), PAGES_CACHE, Optional.empty()));

            addStatusMessage("Book sent successfully");
        }
    }

    public void writeEmptyBook() {
        if (MC.player == null) {
            Psyop.warn("Player is null, not in-game.");
            addStatusMessage("Error: Not in-game");
        } else {
            int slot = InventoryUtils.findAnySlot(Items.WRITABLE_BOOK);
            if (slot == -1) {
                Psyop.warn("No books found.");
                addStatusMessage("Error: No writable books found");
                return;
            }

            if (!InventoryUtils.isHotbarSlot(slot) && slot != 44) {
                InventoryUtils.swapToHotbar(slot, MC.player.getInventory().getSelectedSlot());
                assert MC.player != null;
                slot = MC.player.getInventory().getSelectedSlot();
            }

            if (slot == -1) {
                Psyop.warn("Book was not found.");
                addStatusMessage("Error: Book not found");
                return;
            } else if (InventoryUtils.isHotbarSlot(slot)) {
                InventoryUtils.swapSlot(slot > 8 ? slot - InventoryUtils.getHotbarOffset() : slot);
            }

            PacketUtils.send(new ServerboundEditBookPacket(
                    MC.player.getInventory().getSelectedSlot(), new ArrayList<>(), Optional.empty()));

            addStatusMessage("Empty book sent");
        }
    }

    public void signEmptyBook() {
        if (MC.player == null) {
            Psyop.warn("Player is null, not in-game.");
            addStatusMessage("Error: Not in-game");
        } else {
            int slot = InventoryUtils.findAnySlot(Items.WRITABLE_BOOK);
            if (slot == -1) {
                Psyop.warn("No books found.");
                addStatusMessage("Error: No writable books found");
                return;
            }

            if (!InventoryUtils.isHotbarSlot(slot) && slot != 44) {
                InventoryUtils.swapToHotbar(slot, MC.player.getInventory().getSelectedSlot());
                assert MC.player != null;
                slot = MC.player.getInventory().getSelectedSlot();
            }

            if (slot == -1) {
                Psyop.warn("Book was not found.");
                addStatusMessage("Error: Book not found");
                return;
            } else if (InventoryUtils.isHotbarSlot(slot)) {
                InventoryUtils.swapSlot(slot > 8 ? slot - InventoryUtils.getHotbarOffset() : slot);
            }

            PacketUtils.send(new ServerboundEditBookPacket(
                    MC.player.getInventory().getSelectedSlot(), new ArrayList<>(), Optional.of("")));

            addStatusMessage("Empty book signed");
        }
    }

    @Override
    public void populateSettings(Config conf) {
        this.settings = conf.booksGui;
        this.config = conf.bookSettings;
        Psyop.log("Sign Delay: {}", this.config.signDelay.get());
        executor.scheduleAtFixedRate(
                autoSignTask, config.signDelay.get(), config.signDelay.get(), TimeUnit.SECONDS);
    }

    public enum BookModifier {
        @SerializedName("1")
        RANDOMIZED,
        @SerializedName("2")
        RANDOMIZED_UNICODE,
        @SerializedName("3")
        INVISIBLE,
        @SerializedName("0")
        NONE
    }
}