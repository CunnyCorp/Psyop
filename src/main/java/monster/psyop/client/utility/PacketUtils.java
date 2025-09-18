package monster.psyop.client.utility;

import monster.psyop.client.Liberty;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Dependencies;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Objects;

import static monster.psyop.client.Liberty.EVENT_HANDLER;
import static monster.psyop.client.Liberty.MC;

public class PacketUtils {
    public static boolean NO_SWING = false;
    public static boolean IS_VFP_LOADED = false;
    protected static boolean modifyCurrentTickRots = false;
    protected static long currentTick = 0;
    protected static boolean wasModified = false;
    protected static float modifiedYaw = 0f;
    protected static float modifiedPitch = 0f;

    public static void load() {
        IS_VFP_LOADED = Dependencies.VFP.isLoaded();

        if (IS_VFP_LOADED) {
            Liberty.warn("VFP is loaded, automatically changing how packets are sent.");
        }

        EVENT_HANDLER.add(new PacketUtils());
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        assert MC.player != null;
        if (modifyCurrentTickRots) {
            if (modifiedYaw == MC.player.getYRot() && modifiedPitch == MC.player.getXRot()) {
                return;
            }

            if (event.packet() instanceof ServerboundMovePlayerPacket.Rot packet) {
                event.packet(new ServerboundMovePlayerPacket.Rot(modifiedYaw, modifiedPitch, packet.isOnGround(), packet.horizontalCollision()));
                wasModified = true;
            } else if (event.packet() instanceof ServerboundMovePlayerPacket.PosRot packet) {
                event.packet(new ServerboundMovePlayerPacket.PosRot(packet.getX(MC.player.getX()), packet.getY(MC.player.getY()), packet.getZ(MC.player.getZ()), modifiedYaw, modifiedPitch, packet.isOnGround(), packet.horizontalCollision()));
                wasModified = true;
            } else if (event.packet() instanceof ServerboundMovePlayerPacket.StatusOnly packet) {
                event.packet(new ServerboundMovePlayerPacket.Rot(modifiedYaw, modifiedPitch, packet.isOnGround(), packet.horizontalCollision()));
                wasModified = true;
            } else if (event.packet() instanceof ServerboundMovePlayerPacket.Pos packet) {
                event.packet(new ServerboundMovePlayerPacket.PosRot(packet.getX(MC.player.getX()), packet.getY(MC.player.getY()), packet.getZ(MC.player.getZ()), modifiedYaw, modifiedPitch, MC.player.onGround(), MC.player.horizontalCollision));
                wasModified = true;
            }
        }
    }

    @EventListener
    public void onTickPost(OnTick.Post event) {
        if (!wasModified && modifyCurrentTickRots) {
            assert MC.player != null;
            Liberty.warn("Server wasn't updated on rotations, updating them now.");
            send(new ServerboundMovePlayerPacket.Rot(modifiedYaw, modifiedPitch, MC.player.onGround(), MC.player.horizontalCollision));
            wasModified = false;
        }

        modifyCurrentTickRots = false;
    }


    public static void send(Packet<?> packet) {
        if (MC.player != null) send(MC.player.connection.getConnection(), packet);
        else if (MC.pendingConnection != null) send(MC.pendingConnection, packet);
        else Liberty.warn("Go fuck yourself!");
    }

    public static void send(Connection connection, Packet<?> packet) {
        if (NO_SWING && packet instanceof ServerboundSwingPacket) {
            return;
        }

        if (IS_VFP_LOADED) {
            // Still skips the first layer, but is touchable, but what can we do?
            connection.send(packet, null, true);
        } else {
            connection.channel.writeAndFlush(packet);
        }
    }

    public static void updatePosition(double x, double y, double z) {
        Objects.requireNonNull(MC.player).setPos(x, y, z);
        send(new ServerboundMovePlayerPacket.Pos(x, y, z, EntityUtils.isTouchingGround(), MC.player.horizontalCollision));
    }

    public static void rotate(float pitch, float yaw, boolean update) {
        assert MC.player != null;

        if (update) {
            modifyCurrentTickRots = true;
            modifiedYaw = yaw;
            modifiedPitch = pitch;
        } else {
            MC.player.setXRot(pitch);
            MC.player.setYRot(yaw);
        }
    }

    public static void rotate(float pitch, float yaw) {
        rotate(pitch, yaw, false);
    }

    public static void chat(String string, boolean command) {
        if (command) command(string);
        else chat(string);
    }

    public static void chat(String string) {
        if (string == null
                || string.isBlank()
                || ServerGamePacketListenerImpl.isChatMessageIllegal(string)
                || MC.getConnection() == null) return;

        if (string.startsWith("/")) command(string.replaceFirst("/", ""));
        else MC.getConnection().sendChat(string);
    }

    public static void command(String string) {
        if (string == null
                || string.isBlank()
                || ServerGamePacketListenerImpl.isChatMessageIllegal(string)
                || MC.getConnection() == null) return;

        MC.getConnection().sendCommand(string);
    }
}
