package monster.psyop.client.framework.gui.views.features;

import imgui.ImGui;
import imgui.flag.*;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.gui.LogsSettings;
import monster.psyop.client.config.gui.PersistentGuiSettings;
import monster.psyop.client.framework.gui.utility.ColoredText;
import monster.psyop.client.framework.gui.utility.GuiUtils;
import monster.psyop.client.framework.gui.views.View;
import monster.psyop.client.utility.AnimationUtils;
import monster.psyop.client.utility.PacketUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class ClientLogView extends View {
    public LogsSettings config;
    public static ObjectBigArrayBigList<ColoredText> LOGS = new ObjectBigArrayBigList<>(5000);

    // Animation states
    private final Map<String, Float> commandHoverAnimations = new HashMap<>();
    private long lastFrameTime = System.currentTimeMillis();
    private float inputFocusAnimation = 0f;
    private boolean autoScroll = true;

    // Animation constants
    private static final float ANIMATION_SPEED = 8f;
    private static final float FOCUS_DURATION = 0.3f;

    // Command system
    private final Map<String, Consumer<String[]>> commands = new HashMap<>();

    public ClientLogView() {
        setupCommands();
    }

    @Override
    public String name() {
        return "logs";
    }

    @Override
    public void show() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000f;
        lastFrameTime = currentTime;

        PersistentGuiSettings settings = Config.get().logsGui;

        if (ImGui.begin(displayName(), state(), ImGuiWindowFlags.MenuBar)) {
            if (settings.hasLoaded) {
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Once);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Once);
                ImGui.setWindowSize(settings.width, settings.height, ImGuiCond.Appearing);
                ImGui.setWindowPos(settings.x, settings.y, ImGuiCond.Appearing);
            } else {
                ImGui.setWindowSize(600, 400, ImGuiCond.FirstUseEver);
                ImGui.setWindowPos(0, 0, ImGuiCond.FirstUseEver);
                settings.hasLoaded = true;
            }

            // Menu bar for terminal controls
            if (ImGui.beginMenuBar()) {
                if (ImGui.beginMenu("Terminal")) {
                    if (ImGui.menuItem("Clear", "Ctrl+L")) {
                        LOGS.clear();
                        addLog("Terminal cleared", new Color(0, 200, 0));
                    }
                    if (ImGui.menuItem("Auto-scroll", null, autoScroll)) {
                        autoScroll = !autoScroll;
                    }
                    ImGui.endMenu();
                }
                ImGui.endMenuBar();
            }

            // Apply modern styling
            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 2.0f, 1.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 3.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4.0f, 4.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 4.0f, 2.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.ScrollbarSize, 10.0f);

            // Header with settings
            ImGui.beginChild("header", 0, 30, false);

            // Status indicators
            ImGui.textColored(0.00f, 0.75f, 0.75f, 1.0f, "[ACTIVE]");
            ImGui.sameLine();

            ImGui.textColored(0.90f, 0.90f, 0.95f, 1.0f, "Chat:");
            ImGui.sameLine();
            ImGui.checkbox("##chat", config.chat);
            ImGui.sameLine();

            ImGui.textColored(0.90f, 0.90f, 0.95f, 1.0f, "Commands:");
            ImGui.sameLine();
            ImGui.checkbox("##commands", config.commands);

            ImGui.endChild();

            ImGui.separator();

            // Log messages area
            if (ImGui.beginChild("messages", 0, -30, true)) {
                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 1.0f, 0.0f);

                // Display logs in correct order (newest at bottom)
                for (long i = 0; i < LOGS.size64(); i++) {
                    ColoredText log = LOGS.get(i);

                    // Add prompt style for commands
                    if (log.text().startsWith("> ")) {
                        ImGui.textColored(0.00f, 0.75f, 0.75f, 1.0f, ">");
                        ImGui.sameLine(0, 2);
                        GuiUtils.text(new ColoredText(
                                log.text().substring(2),
                                new Color(0, 180, 180)
                        ));
                    } else {
                        GuiUtils.text(log);
                    }
                }

                // Auto-scroll to bottom if enabled
                if (autoScroll && ImGui.getScrollY() >= ImGui.getScrollMaxY() - 5) {
                    ImGui.setScrollHereY(1.0f);
                }

                ImGui.popStyleVar();
            }
            ImGui.endChild();

            // Command input area
            ImGui.beginChild("input_area", 0, 0, false);

            // Input prompt
            float blink = (float) Math.abs(Math.sin(currentTime * 0.005));
            ImGui.textColored(0.00f, 0.75f, 0.75f, blink, ">");
            ImGui.sameLine();

            // Input focus animation
            boolean inputFocused = ImGui.isItemFocused();
            inputFocusAnimation = AnimationUtils.animate(currentTime,
                    inputFocused ? currentTime : currentTime - (long) (FOCUS_DURATION * 1000),
                    FOCUS_DURATION,
                    inputFocused ? AnimationUtils.CUBIC_OUT : AnimationUtils.CUBIC_IN);

            ImGui.pushStyleColor(ImGuiCol.FrameBg,
                    AnimationUtils.lerpColor(
                            ImGui.getColorU32(ImGuiCol.FrameBg),
                            ImGui.getColorU32(ImGuiCol.FrameBgActive),
                            inputFocusAnimation
                    )
            );

            // Input text
            ImGui.pushItemWidth(-1);
            boolean inputActivated = ImGui.inputText("##command_input", config.input,
                    ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.popItemWidth();
            ImGui.popStyleColor();

            // Input handling
            if (inputActivated) {
                handleInput(config.input.get().trim());
                config.input.set("");
            }

            ImGui.endChild();

            ImGui.popStyleVar(5);
        }

        settings.x = ImGui.getWindowPosX();
        settings.y = ImGui.getWindowPosY();
        settings.width = ImGui.getWindowWidth();
        settings.height = ImGui.getWindowHeight();
        ImGui.end();
    }

    private void handleInput(String input) {
        if (input.isEmpty()) return;

        if (input.startsWith("/")) {
            // Handle command
            String commandText = input.substring(1).trim();
            String[] parts = commandText.split("\\s+", 2);
            String commandName = parts[0].toLowerCase();
            String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

            Consumer<String[]> commandHandler = commands.get(commandName);
            if (commandHandler != null) {
                commandHandler.accept(args);
                addLog("> " + input, new Color(0, 200, 200)); // Cyan for commands
            } else {
                addLog("Unknown command: " + commandName, new Color(255, 100, 100));
            }
        } else if (config.chat.get()) {
            // Handle chat message
            boolean fricked = false;
            if (input.startsWith("/") && !config.commands.get()) {
                addLog("Commands are disabled!", new Color(255, 150, 0));
                fricked = true;
            }

            boolean isCommand = input.startsWith("/");
            if (!fricked && isCommand && config.commands.get()) {
                PacketUtils.command(input.substring(1));
                addLog("> " + input, new Color(0, 180, 180)); // Cyan for system commands
            } else if (!fricked && !isCommand) {
                PacketUtils.chat(input, false);
                addLog("> " + input, new Color(150, 220, 150)); // Light green for chat
            }
        } else {
            addLog("Chat is disabled!", new Color(255, 150, 0));
        }
    }

    private void addLog(String message, Color color) {
        LOGS.add(new ColoredText(message, color));
        // Keep log size manageable
        if (LOGS.size64() > 1000) {
            LOGS.remove(0);
        }
    }

    private void setupCommands() {
        // Help command
        commands.put("help", args -> {
            addLog("Available commands:", new Color(0, 180, 220));
            List<String> cmdList = new ArrayList<>(commands.keySet());
            Collections.sort(cmdList);
            for (String cmd : cmdList) {
                addLog("  /" + cmd, new Color(0, 160, 200));
            }
        });

        // Clear command
        commands.put("clear", args -> {
            LOGS.clear();
            addLog("Terminal cleared", new Color(0, 200, 0));
        });

        // Info command
        commands.put("info", args -> {
            addLog("Shondo Client v1.0.0", new Color(0, 180, 220));
            addLog("Terminal Mode: ACTIVE", new Color(0, 200, 0));
            addLog("Security: ENABLED", new Color(0, 220, 150));
        });

        // Echo command
        commands.put("echo", args -> {
            if (args.length > 0) {
                addLog(String.join(" ", args), new Color(0, 220, 150));
            } else {
                addLog("Usage: /echo <message>", new Color(255, 180, 0));
            }
        });

        // Ping command
        commands.put("ping", args -> {
            addLog("Pong! Connection established.", new Color(0, 200, 0));
        });

        // Modules command
        commands.put("modules", args -> {
            addLog("Active modules:", new Color(0, 220, 150));
            /*Shondo.MODULES.getModules().stream()
                .filter(Module::active)
                .forEach(module ->
                    addLog("- " + module.getLabel(), new Color(0, 200, 100))
                );*/
        });

        // System command
        commands.put("sys", args -> {
            addLog("System diagnostics:", new Color(0, 180, 220));
            addLog("Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + "MB/" +
                    Runtime.getRuntime().totalMemory() / 1048576 + "MB", new Color(0, 200, 200));
            addLog("Processors: " + Runtime.getRuntime().availableProcessors(), new Color(0, 200, 200));
        });

        // Scan command
        commands.put("scan", args -> {
            addLog("Initiating network scan...", new Color(0, 200, 0));
            addLog("Targets located: 7", new Color(0, 200, 150));
            addLog("Vulnerabilities detected: 3", new Color(0, 220, 150));
            addLog("Firewall bypassed", new Color(0, 200, 0));
        });
    }

    @Override
    public void populateSettings(Config config) {
        settings = config.logsGui;
        this.config = config.logsSettings;
    }
}