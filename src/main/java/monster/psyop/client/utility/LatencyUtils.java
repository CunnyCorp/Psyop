package monster.psyop.client.utility;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnScreen;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;

import java.util.ArrayList;

import static monster.psyop.client.Psyop.MC;

public class LatencyUtils {
    public static final ArrayList<Integer> pings = new ArrayList<>();
    private static boolean expectingPing = false;
    private static long lastRequested = 0;
    private static int ping = 0;

    public static void addPing(int ping) {
        if (pings.size() >= 60) pings.remove(pings.size() - 1);
        pings.add(0, ping);
    }

    public static void preformPing() {
        if (expectingPing || MC.player == null || MC.isSingleplayer()) return;
        assert MC.player != null;
        PacketUtils.send(
                new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
        expectingPing = true;
    }

    public static void calculateLatency() {
        if (pings.size() < 10) {
            ping = 0;
            return;
        }

        int sum = 0;
        for (int i : pings) {
            sum += i;
        }
        ping = sum / pings.size();
    }

    public static int getLatency() {
        return ping;
    }

    @EventListener
    public void onPacketReceive(OnPacket.Received event) {
        if (event.packet() instanceof ClientboundAwardStatsPacket && lastRequested != 0) {
            addPing(Math.toIntExact(System.currentTimeMillis() - lastRequested));
            lastRequested = 0;
        }
    }

    @EventListener
    public void onPacketSent(OnPacket.Sent event) {
        if (event.packet() instanceof ServerboundClientCommandPacket packet) {
            if (packet.getAction() == ServerboundClientCommandPacket.Action.REQUEST_STATS
                    && expectingPing) {
                lastRequested = System.currentTimeMillis();
                expectingPing = false;
            }
        }
    }

    @EventListener
    public void onScreenChange(OnScreen.Open event) {
        lastRequested = 0;
        expectingPing = false;
    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        preformPing();
    }
}
