package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.Liberty;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingSpoof extends Module {
    public ScheduledExecutorService PING_PONG_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    public ScheduledExecutorService KEEP_ALIVE_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public final IntSetting ping =
            new IntSetting.Builder()
                    .name("ping")
                    .description("The amount of ping to spoof up by.")
                    .defaultTo(25)
                    .range(1, 30000)
                    .addTo(coreGroup);
    public final BoolSetting jitter =
            new BoolSetting.Builder()
                    .name("jitter")
                    .description("Jitters ping by random amounts.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final FloatSetting jitterChance =
            new FloatSetting.Builder()
                    .name("jitter-chance")
                    .description("Chances of applying jitters.")
                    .defaultTo(0.15f)
                    .range(0.0f, 1.0f)
                    .visible((v) -> jitter.get())
                    .addTo(coreGroup);
    public final IntSetting jitterMin =
            new IntSetting.Builder()
                    .name("jitter-min")
                    .description("The lower boundary of jitters.")
                    .defaultTo(5)
                    .range(1, 30000)
                    .visible((v) -> jitter.get())
                    .addTo(coreGroup);
    public final IntSetting jitterMax =
            new IntSetting.Builder()
                    .name("jitter-max")
                    .description("The upper boundary of jitters, min + max.")
                    .defaultTo(10)
                    .range(1, 30000)
                    .visible((v) -> jitter.get())
                    .addTo(coreGroup);
    public final BoolSetting pingPong =
            new BoolSetting.Builder()
                    .name("ping-pong")
                    .description("Spoofs pings in response to pongs.")
                    .defaultTo(true)
                    .addTo(coreGroup);

    public PingSpoof() {
        super(Categories.MISC, "ping-spoof", "Allows you to increase ping.");
    }

    @EventListener
    public void onPacketReceived(OnPacket.Received event) {
        if (pingPong.value().get() && event.packet() instanceof ClientboundPingPacket packet) {
            PING_PONG_EXECUTOR.schedule(() -> {
                if (MC.getConnection() != null) MC.getConnection().handlePing(packet);
            }, getDelay(), TimeUnit.MILLISECONDS);

            event.cancel();
        }
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundKeepAlivePacket packet) {
            KEEP_ALIVE_EXECUTOR.schedule(() -> {
                if (MC.player != null) {
                    PacketUtils.send(new ServerboundKeepAlivePacket(packet.getId()));
                }
            }, getDelay(), TimeUnit.MILLISECONDS);

            event.cancel();
        }
    }

    private int getDelay() {
        int i = ping.get();

        if (jitter.get() && Liberty.RANDOM.nextFloat() <= jitterChance.get()) {
            i += Liberty.RANDOM.nextInt(jitterMin.get(), jitterMin.get() + jitterMax.get());
        }

        return i;
    }
}
