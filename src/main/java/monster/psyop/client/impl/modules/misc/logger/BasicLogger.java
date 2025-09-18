package monster.psyop.client.impl.modules.misc.logger;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.PathIndex;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BasicLogger extends Module {
    private final BoolSetting fileLog =
            new BoolSetting.Builder()
                    .name("file-logging")
                    .description("Logs changes to a file instead of using normal logging.")
                    .defaultTo(false)
                    .addTo(coreGroup);
    private final GroupedSettings serverBoundGroup = addGroup(new GroupedSettings("server-bound", "Information sent from the client or otherwise unrelated to the server."));
    private final BoolSetting blockInteractions =
            new BoolSetting.Builder()
                    .name("block-interactions")
                    .description("Logs block interactions.")
                    .defaultTo(false)
                    .addTo(serverBoundGroup);
    private final BoolSetting rotChanges =
            new BoolSetting.Builder()
                    .name("rotations")
                    .description("Logs changes in rotations.")
                    .defaultTo(false)
                    .addTo(serverBoundGroup);
    private final BoolSetting moveStatus =
            new BoolSetting.Builder()
                    .name("move-status")
                    .description("Logs movement status updates.")
                    .defaultTo(false)
                    .addTo(serverBoundGroup);
    private final BoolSetting yChanges =
            new BoolSetting.Builder()
                    .name("y-changes")
                    .description("Logs Y changes in movement.")
                    .defaultTo(false)
                    .addTo(serverBoundGroup);

    protected static Path logPath = PathIndex.LOGS;
    private float lastPitch = -777;
    private float lastYaw = -777;
    private double lastY = -777;
    private boolean lastHC = false;
    private boolean lastOG = false;

    public BasicLogger() {
        super(Categories.MISC, "basic-logger", "Lets you log various things, for debugging.");

        try {
            Files.createDirectories(PathIndex.LOGS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logPath = logPath.resolve(System.currentTimeMillis() + ".log");

    }


    @EventListener
    private void onPacketSnooSnoo(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundContainerClickPacket packet) {
            LoggingUtils.log("Click Slot", "Clicking: {}, {}", packet.slotNum(), packet.clickType().name());
            return;
        }

        if (event.packet() instanceof ServerboundUseItemOnPacket packet) {
            if (blockInteractions.get()) {

            }
        }

        if (event.packet() instanceof ServerboundMovePlayerPacket packet) {
            if (moveStatus.get()) {
                if (packet.horizontalCollision() != lastHC) {
                    LoggingUtils.log("Horizontal Collision", "{} -> {}", lastHC, packet.horizontalCollision());
                    lastHC = packet.horizontalCollision();
                }

                if (packet.isOnGround() != lastOG) {
                    LoggingUtils.log("On Ground", "{} -> {}", lastOG, packet.isOnGround());
                    lastOG = packet.isOnGround();
                }
            }

            if (rotChanges.get() && packet.hasRotation()) {
                float yaw = packet.getYRot(0);
                float pitch = packet.getXRot(0);

                if (yaw != lastYaw || pitch != lastPitch) {
                    LoggingUtils.log("Rotations", "Yaw: {} -> {}; Pitch: {} -> {}; Diff: {}. {}", lastYaw, yaw, lastPitch, pitch, lastYaw - yaw, lastPitch - pitch);

                    this.lastPitch = yaw;
                    this.lastYaw = pitch;
                }
            }

            if (yChanges.get() && packet.hasPosition()) {
                double y = packet.getY(-909999999);

                if (y != lastY) {
                    LoggingUtils.log("Y", "{} -> {}; Diff: {}", lastY, y, lastY - y);
                    lastY = y;
                }
            }
        }
    }
}
