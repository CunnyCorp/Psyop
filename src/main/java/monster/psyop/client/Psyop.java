package monster.psyop.client;

import monster.psyop.client.config.Config;
import monster.psyop.client.framework.events.EventHandler;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.gui.Gui;
import monster.psyop.client.framework.gui.utility.ColoredText;
import monster.psyop.client.framework.gui.views.ViewHandler;
import monster.psyop.client.framework.gui.views.client.ModuleConfigView;
import monster.psyop.client.framework.gui.views.client.ModulesView;
import monster.psyop.client.framework.gui.views.features.*;
import monster.psyop.client.framework.modules.*;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.eggs.EggLoader;
import monster.psyop.client.impl.modules.chat.AutoGroom;
import monster.psyop.client.impl.modules.chat.BetterChat;
import monster.psyop.client.impl.modules.chat.Spammer;
import monster.psyop.client.impl.modules.client.AntiCheatModule;
import monster.psyop.client.impl.modules.client.ConfigModule;
import monster.psyop.client.impl.modules.client.RenderTweaks;
import monster.psyop.client.impl.modules.combat.*;
import monster.psyop.client.impl.modules.exploits.*;
import monster.psyop.client.impl.modules.hud.*;
import monster.psyop.client.impl.modules.misc.*;
import monster.psyop.client.impl.modules.movement.*;
import monster.psyop.client.impl.modules.player.*;
import monster.psyop.client.impl.modules.render.*;
import monster.psyop.client.impl.modules.silly.HappyHands;
import monster.psyop.client.impl.modules.silly.OiledUp;
import monster.psyop.client.impl.modules.vhud.ArmorHUD;
import monster.psyop.client.impl.modules.vhud.InventoryHUD;
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

    public static void postInit() {
        GUI = new Gui();
        GUI.launch();

        PacketUtils.load();
        McDataCache.load();
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
        if (!DEBUGGING && !ConfigModule.isDebugging()) {
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

        FriendManager.load();
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

                                    for (JavaAddon addon : ADDON_MANAGER.plugins.values()) {
                                        addon.onShutdown();
                                    }
                                }));

        ADDON_MANAGER.findPlugins();


        // Views
        new ModulesView().load();
        new ModuleConfigView().load();
        new ClientLogView().load();
        new FriendsView().load();
        new BookEditorView().load();
        new TrollingView().load();

        // Chat
        new AutoGroom().load();
        new BetterChat().load();
        new Spammer().load();

        // Combat
        new AntiKb().load();
        new AttackBurst().load();
        new AutoElytraSwap().load();
        new AutoPhase().load();
        new KillAura().load();

        // Exploits
        new FastSwap().load();
        new HoldPackets().load();
        new InstantClose().load();
        new LoginFuckery().load();
        new QueueBypass().load();
        new SilentClose().load();
        new HostOverride().load();

        // HUD
        new ArmorHUD().load();
        new ArrayHUD().load();
        new HudEditor().load();
        new InventoryHUD().load();
        new LookingAtHUD().load();
        new NotificationHUD().load();
        new PlayerRadarHUD().load();
        new PositionHUD().load();
        new TargetHUD().load();

        // Misc
        new AntiNarrator().load();
        new BrandSpoof().load();
        new DetachMouse().load();
        new DiscordRPC().load();
        new Friends().load();
        new InventoryPanel().load();
        new NoMiss().load();
        new NoSwing().load();
        new PingSpoof().load();
        new Rotation().load();
        new UseControl().load();

        // Player
        new AutoEat().load();
        new AutoTool().load();
        new EffectSpoof().load();
        new FastUse().load();
        new GuiMove().load();
        new MiddleClickUse().load();
        new Offhand().load();
        new Reach().load();


        // Movement
        new AntiPush().load();
        new AutoWalk().load();
        new ElytraPause().load();
        new Glide().load();
        new GrimBunnyHop().load();
        new Jumping().load();
        new LagbackDetector().load();
        new NoSlow().load();
        new Phase().load();
        new PlayerTimer().load();
        new Sneak().load();
        new SpinBot().load();
        new Sprint().load();
        new Speed().load();

        // Render
        new AntiBlinker().load();
        new ArmorView().load();
        new BetterTab().load();
        new BlockLights().load();
        new BoxESP().load();
        new Chams().load();
        new HandView().load();
        new HitTrack().load();
        new ItemESP().load();
        new ItemView().load();
        new NoRender().load();
        new ParticleEngine().load();
        new PopESP().load();
        new PumpkinSpoof().load();
        new Ripples().load();
        new SphereESP().load();
        new StorageESP().load();
        new Tint().load();
        new Trail().load();
        new WorldView().load();
        new VisualRange().load();

        // Silly
        new HappyHands().load();
        new OiledUp().load();

        // World
        if (Dependencies.LITEMATICA.isLoaded()) {
            if (Dependencies.BARITONE.isLoaded() || Dependencies.BARITONE_METEOR.isLoaded()) {
                new Printer().load();
                new SkyRefill().load();
            }
        }
        new Nuker().load();
        new Painter().load();
        new BreakDelay().load();
        new FastBreak().load();
        new Scaffold().load();
        new SignFucker().load();

        // Client
        new AntiCheatModule().load();
        new ConfigModule().load();
        new RenderTweaks().load();

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

        ViewHandler.get(FriendsView.class).populateSettings(CONFIG);

        Psyop.log("Took {} nanoseconds to load.", System.nanoTime() - loadedStart);
    }
}
