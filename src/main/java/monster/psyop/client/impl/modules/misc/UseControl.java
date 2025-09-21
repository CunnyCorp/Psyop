package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;

import java.util.Arrays;

public class UseControl extends Module {
    public final BoolSetting useAction =
            new BoolSetting.Builder()
                    .name("use-action")
                    .description("Controls the secondary action.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public final BoolSetting secondary =
            new BoolSetting.Builder()
                    .name("secondary")
                    .description("Whether or not to use the secondary action.")
                    .defaultTo(false)
                    .visible((v) -> useAction.value().get())
                    .addTo(coreGroup);
    public final BoolSetting remount =
            new BoolSetting.Builder()
                    .name("remount")
                    .description("Attempts to remount automatically.")
                    .defaultTo(false)
                    .addTo(coreGroup);

    private int lastKnownVehicle = 0;
    private int vehicleTicks = 0;

    public UseControl() {
        super(Categories.MISC, "use-control", "Modifies how blocks/entities are interacted with.");
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (useAction.get() && event.packet() instanceof ServerboundInteractPacket packet) {
            event.packet(new ServerboundInteractPacket(packet.entityId, secondary.get(), packet.action));
        }
    }

    @Override
    public void update() {
        if (MC.player.getVehicle() != null) {
            if (lastKnownVehicle != MC.player.getVehicle().getId()) {
                vehicleTicks = 0;
            }

            lastKnownVehicle = MC.player.getVehicle().getId();
            vehicleTicks++;
        } else {
            if (vehicleTicks > 5) {
                vehicleTicks = 0;
                lastKnownVehicle = 0;
            }
        }
    }

    @EventListener
    public void onPacketReceived(OnPacket.Received event) {
        if (remount.get() && event.packet() instanceof ClientboundSetPassengersPacket packet) {
            if (lastKnownVehicle == packet.getVehicle()) {
                if (Arrays.stream(packet.getPassengers()).noneMatch((s) -> MC.player.getId() == s)) {
                    PacketUtils.send(new ServerboundInteractPacket(packet.getVehicle(), false, new ServerboundInteractPacket.InteractionAction(InteractionHand.OFF_HAND)));
                }
            }
        }
    }
}
