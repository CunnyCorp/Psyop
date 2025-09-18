package monster.psyop.client.impl.modules.misc.logger;

import monster.psyop.client.config.Config;
import monster.psyop.client.utility.StringUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Objects;

public class LoggingUtils {
    private static final HashMap<Class<? extends Packet<?>>, PacketConversion> PACKET_CONVERSIONS = new HashMap<>();
    private static final HashMap<String, PacketType<?>> STRING_TO_TYPE = new HashMap<>();

    public static void init() {
        PACKET_CONVERSIONS.put(ServerboundUseItemOnPacket.class, new PacketConversion(p -> {
            ServerboundUseItemOnPacket packet = (ServerboundUseItemOnPacket) p;

            log(StringUtils.readable(p.type().id().toString(), Config.get().coreSettings), hitResultToString(packet.getHitResult()) + " - " + packet.getHand().name() + " - " + packet.getSequence());
        }));

        PACKET_CONVERSIONS.put(ServerboundContainerClickPacket.class, new PacketConversion(p -> {
            ServerboundContainerClickPacket packet = (ServerboundContainerClickPacket) p;
            String result = packet.containerId() + " - " +
                    packet.stateId() + " - " +
                    packet.slotNum() + " - " +
                    packet.buttonNum() + " - " +
                    packet.carriedItem() + " - ";

            log(StringUtils.readable(p.type().id().toString(), Config.get().coreSettings), result);
        }));
    }

    @Nullable
    public static PacketType<?> getById(String str) {
        return STRING_TO_TYPE.get(str);
    }

    public static String getStringByType(PacketType<?> packetType) {
        return packetType.id().toString();
    }

    protected static void log(String pfx, String msg, Object... args) {
        String content = "[" + pfx + "] " + format(msg, args);
        content += "\n";

        try {
            Files.write(
                    BasicLogger.logPath,
                    content.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String format(String str, Object... args) {
        if (str.contains("{}")) {
            for (Object arg : args) {
                if (!str.contains("{}")) break;

                str = str.replaceFirst("\\{}", arg.toString());
            }
        }

        return str;
    }


    public static void tryLogging(Packet<?> packet) {
        if (isPacketSupported(packet)) {
            PACKET_CONVERSIONS.get(packet.getClass()).safeCall(packet);
        }
    }

    public static boolean isPacketSupported(Packet<?> packet) {
        return PACKET_CONVERSIONS.containsKey(packet.getClass());
    }

    private static String hitResultToString(HitResult hitResult) {
        StringBuilder result = new StringBuilder();
        result.append(hitResult.getType().name()).append(" | ");
        result.append(hitResult.getLocation().x).append(",").append(hitResult.getLocation().y).append(",").append(hitResult.getLocation().z).append(" | ");

        if (hitResult instanceof BlockHitResult blockHitResult) {
            result.append("(").append(blockHitResult.getBlockPos().toShortString()).append(" | ").append(blockHitResult.getDirection().name()).append(" | ").append(blockHitResult.isInside()).append(")");
        }

        if (hitResult instanceof EntityHitResult entityHitResult) {
            result.append("(");
            if (entityHitResult.getEntity().hasCustomName()) {
                result.append(Objects.requireNonNull(entityHitResult.getEntity().getCustomName()).getString());
            }


            result.append(" | ").append(entityHitResult.getEntity().getUUID());

            result.append(" | ").append(entityHitResult.getEntity().getType().getDescriptionId());

            result.append(")");
        }

        return result.toString();
    }
}
