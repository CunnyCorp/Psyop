package monster.psyop.client.impl.modules.misc;

import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.StringSetting;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class DiscordRPC extends Module {
    private static final long CLIENT_ID = 1425266312287879188L;

    private static DiscordRPC INSTANCE;

    private final RichPresence presence = new RichPresence();

    private Timer timer;
    private long lastStateChange = 0;
    private long lastUpdate = 0;
    private int currentState = 0;

    private final StringSetting details =
            new StringSetting.Builder()
                    .name("details")
                    .description("The first line of text. Placeholders: {username}, {server}")
                    .defaultTo("IGN: {username}")
                    .addTo(coreGroup);

    private final StringSetting state1 =
            new StringSetting.Builder()
                    .name("state-1")
                    .description("The first line of looping text. Placeholders: {players}, {server}, {username}")
                    .defaultTo("Playing on {server}")
                    .addTo(coreGroup);

    private final StringSetting state2 =
            new StringSetting.Builder()
                    .name("state-2")
                    .description("The second line of looping text. Placeholders: {players}, {server}, {username}")
                    .defaultTo("{players} online")
                    .addTo(coreGroup);

    private final StringSetting largeImageText =
            new StringSetting.Builder()
                    .name("large-image-text")
                    .description("The text that appears when hovering over the large image.")
                    .defaultTo("Psyop Client")
                    .addTo(coreGroup);

    private final IntSetting updateInterval =
            new IntSetting.Builder()
                    .name("update-interval")
                    .description("How often to update the presence in seconds.")
                    .defaultTo(5)
                    .range(1, 60)
                    .addTo(coreGroup);

    private final IntSetting loopDelay =
            new IntSetting.Builder()
                    .name("loop-delay")
                    .description("The delay in seconds between looping text.")
                    .defaultTo(5)
                    .range(1, 60)
                    .addTo(coreGroup);

    public DiscordRPC() {
        super(Categories.MISC, "discord-rpc", "Shows your status on Discord.", DiscordRPC::startRpc, DiscordRPC::stopRpc);

        INSTANCE = this;
    }

    private static void startRpc() {
        if (INSTANCE == null) return;

        try {
            boolean started = DiscordIPC.start(CLIENT_ID, () -> System.out.println("Logged in account: " + DiscordIPC.getUser().username));
            if (!started) {
                System.out.println("Failed to start Discord IPC");
                return;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }

        INSTANCE.presence.setStart(Instant.now().getEpochSecond());

        INSTANCE.timer = new Timer("DiscordRPC-Timer", true);
        INSTANCE.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (INSTANCE == null) return;
                    if (!INSTANCE.active()) return;

                    long now = System.currentTimeMillis();
                    if (now - INSTANCE.lastUpdate >= (long) INSTANCE.updateInterval.get() * 1000L) {
                        INSTANCE.lastUpdate = now;
                        INSTANCE.doUpdate();
                    }

                    if (now - INSTANCE.lastStateChange >= (long) INSTANCE.loopDelay.get() * 1000L) {
                        INSTANCE.currentState = (INSTANCE.currentState + 1) % 2;
                        INSTANCE.lastStateChange = now;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }, 0L, 1000L);
    }

    private static void stopRpc() {
        if (INSTANCE == null) return;
        try {
            if (INSTANCE.timer != null) {
                INSTANCE.timer.cancel();
                INSTANCE.timer = null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            DiscordIPC.stop();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    private void doUpdate() {
        try {
            try {
                DiscordIPC.getUser();
            } catch (Throwable t) {
                return;
            }

            String username = "Unknown";
            String server = "In the menus";
            String players = "N/A";
            boolean inGame = false;

            try {
                if (MC != null && MC.player != null) {
                    inGame = true;
                    username = MC.player.getGameProfile().getName();
                    server = (MC.getCurrentServer() != null) ? MC.getCurrentServer().ip : "Singleplayer";
                    players = (MC.player.connection != null) ? MC.player.connection.getOnlinePlayers().size() + " players" : "N/A";
                } else if (MC != null && MC.getUser() != null) {
                    username = MC.getUser().getName();
                }
            } catch (Throwable t) {}

            String detailsText = details.value().get()
                    .replace("{username}", username)
                    .replace("{server}", server)
                    .replace("{players}", players);

            String stateText;
            if (inGame) {
                String stateTemplate = (currentState == 0) ? state1.value().get() : state2.value().get();
                stateText = stateTemplate
                        .replace("{username}", username)
                        .replace("{server}", server)
                        .replace("{players}", players);
            } else {
                stateText = "In the main menu";
            }

            presence.setDetails(detailsText);
            presence.setState(stateText);
            presence.setLargeImage("psyoplogo", largeImageText.value().get());

            DiscordIPC.setActivity(presence);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void update() {}
}