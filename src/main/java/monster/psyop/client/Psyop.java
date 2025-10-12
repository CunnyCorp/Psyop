package monster.psyop.client;

import monster.psyop.client.config.Config;
import monster.psyop.client.framework.AnActualRat;
import monster.psyop.client.framework.events.EventHandler;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.gui.Gui;
import monster.psyop.client.framework.gui.utility.ColoredText;
import monster.psyop.client.framework.gui.views.client.ConfigView;
import monster.psyop.client.framework.gui.views.client.ModuleConfigView;
import monster.psyop.client.framework.gui.views.client.ModulesView;
import monster.psyop.client.framework.gui.views.features.BookEditorView;
import monster.psyop.client.framework.gui.views.features.ClientLogView;
import monster.psyop.client.framework.gui.views.features.TrollingView;
import monster.psyop.client.framework.modules.*;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.eggs.EggLoader;
import monster.psyop.client.impl.modules.chat.BetterChat;
import monster.psyop.client.impl.modules.combat.*;
import monster.psyop.client.impl.modules.exploits.*;
import monster.psyop.client.impl.modules.hud.*;
import monster.psyop.client.impl.modules.misc.*;
import monster.psyop.client.impl.modules.movement.*;
import monster.psyop.client.impl.modules.player.*;
import monster.psyop.client.impl.modules.render.*;
import monster.psyop.client.impl.modules.silly.HappyHands;
import monster.psyop.client.impl.modules.silly.OiledUp;
import monster.psyop.client.impl.modules.world.*;
import monster.psyop.client.impl.modules.world.printer.Printer;
import monster.psyop.client.impl.modules.world.printer.SkyRefill;
import monster.psyop.client.plugins.AddonManager;
import monster.psyop.client.plugins.JavaAddon;
import monster.psyop.client.utility.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Psyop implements ModInitializer {
    public static final RandomSource RANDOM = RandomSource.create();
    public static final Logger LOG = LoggerFactory.getLogger("Psyop");
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);
    public static final AddonManager ADDON_MANAGER = new AddonManager();
    public static final EventHandler EVENT_HANDLER = new EventHandler();
    public static final Modules MODULES = new Modules();
    public static Minecraft MC;
    public static Psyop INSTANCE;
    public static Gui GUI;
    public static Config CONFIG;
    public static boolean DEBUGGING = false;
    public static Thread RENDERING_THREAD;

    public static void postInit() {
        /*RENDERING_THREAD = new Thread(() -> {
            GUI = new Gui();
            GUI.launch();

            GUI.run();
        });

        RENDERING_THREAD.setName("749 Rendering");
        RENDERING_THREAD.start();
         */

        GUI = new Gui();
        GUI.launch();

        PacketUtils.load();

        McDataCache.init();

        AnActualRat.actuallyJustRatYourself();

        WorldUtils.load();
    }

    public static void log(Color color, String str, Object... args) {
        if (str.contains("{}")) {
            for (Object arg : args) {
                str = str.replaceFirst("\\{}", arg.toString());
            }
        }

        if (ClientLogView.LOGS.size64() >= 5000) ClientLogView.LOGS.remove(0);

        String finalStr = str;

        if (ClientLogView.LOGS.isEmpty() || !Objects.equals(ClientLogView.LOGS.get(ClientLogView.LOGS.size64() - 1).text(), finalStr)) {
            ClientLogView.LOGS.removeIf(coloredText -> coloredText.text().equals(finalStr));
            ClientLogView.LOGS.add(new ColoredText(str, color));
        }

        LOG.info("[Psyop] {}", str);
    }

    public static void log(String str, Object... args) {
        log(Color.WHITE, str, args);
    }

    public static void debug(String str, Object... args) {
        if (!DEBUGGING) {
            return;
        }

        log(Color.YELLOW, str, args);
    }

    public static void warn(String str, Object... args) {
        log(Color.ORANGE, str, args);
    }

    public static void error(String str, Object... args) {
        log(Color.RED, str, args);
    }

    @Override
    public void onInitialize() {
        long loadedStart = System.nanoTime();

        INSTANCE = this;

        FriendManager.init();

        new StringUtils();

        MC = Minecraft.getInstance();

        CONFIG = new Config();
        CONFIG.load();

        if (Files.exists(PathIndex.CLIENT.resolve("Whos a good girl"))) {
            DEBUGGING = true;
            debug("You're a good girl! Debug!");
        }

        if (DEBUGGING && Files.exists(PathIndex.CLIENT.resolve("Im a bad girl"))) {
            DEBUGGING = false;
            error("Five lashings for you!");
        }


        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    if (CONFIG != null) {
                                        CONFIG.save();
                                    }

                                    if (RENDERING_THREAD != null) {
                                        RENDERING_THREAD.interrupt();
                                    }

                                    for (JavaAddon addon : ADDON_MANAGER.plugins.values()) {
                                        addon.onShutdown();
                                    }
                                }));

        ADDON_MANAGER.findPlugins();


        // Views
        new ConfigView().load();
        new ModulesView().load();
        new ModuleConfigView().load();
        new ClientLogView().load();
        new BookEditorView().load();
        new TrollingView().load();

        // Chat
        new BetterChat().load();

        // HUD
        new ArmorHUD().load();
        new ArrayHUD().load();
        new PositionHUD().load();
        new TargetHUD().load();
        new NotificationHUD().load();
        new PlayerRadarHUD().load();

        // Combat
        new AntiKb().load();
        new AttackBurst().load();
        new KillAura().load();
        new TriggerBot().load();

        // Exploits
        new FastSwap().load();
        new InstantClose().load();
        new QueueBypass().load();
        new SilentClose().load();
        new LoginFuckery().load();
        new HoldPackets().load();

        // Misc
        new AutoEat().load();
        new NoMiss().load();
        new FastUse().load();
        new BrandSpoof().load();
        new DetachMouse().load();
        new PingSpoof().load();
        new Reach().load();
        new Rotation().load();
        new TestModule().load();
        new UseControl().load();
        new Friends().load();
        new AutoElytraSwap().load();
        new DiscordRPC().load();
        new EffectSpoof().load();
        new AutoTool().load();

        // Movement
        new AntiPush().load();
        new AutoWalk().load();
        new Jumping().load();
        new Phase().load();
        new Sneak().load();
        new GrimBunnyHop().load();
        new PlayerTimer().load();
        new Sprint().load();
        new SpinBot().load();
        new ElytraPause().load();
        new SillyBot().load();
        new LagbackDetector().load();

        // Render
        new BlockLights().load();
        new Chams().load();
        new HandView().load();
        new HideArmor().load();
        new ItemView().load();
        new WorldView().load();
        new ESP().load();
        new StorageESP().load();
        new Ripples().load();
        new BetterTab().load();
        new Trail().load();

        // Silly
        new HappyHands().load();
        new OiledUp().load();

        // World
        if (Dependencies.LITEMATICA.isLoaded()) {
            if (Dependencies.BARITONE.isLoaded()) {
                new Printer().load();
                new SkyRefill().load();
            }
        }
        new AutoMine().load();
        new BreakDelay().load();
        new FastBreak().load();
        new Scaffold().load();
        new SignFucker().load();

        EggLoader.loadEggs();

        EVENT_HANDLER.add(new LatencyUtils());

        // Calls preload methods.
        ADDON_MANAGER.initialize();

        // Populate modules.
        for (Category category : Categories.INDEX) {
            for (Module module : MODULES.getModules(category)) {
                try {
                    Config.get().populateModule(module);
                } catch (RuntimeException exception) {
                    Psyop.LOG.error("The module {} failed to populate configurations.", module.name, exception);
                    Config.get().modules.remove(module.name);
                }
            }
        }


        Psyop.log("Took {} nanoseconds to load.", System.nanoTime() - loadedStart);
    }
}
