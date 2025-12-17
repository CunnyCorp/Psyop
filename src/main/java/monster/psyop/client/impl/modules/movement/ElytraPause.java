package monster.psyop.client.impl.modules.movement;

import imgui.ImGui;
import imgui.type.ImInt;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.KeybindingSetting;
import monster.psyop.client.impl.events.game.OnMove;
import monster.psyop.client.utility.MathUtils;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

public class ElytraPause extends Module {
    private final BoolSetting liquidCheck = new BoolSetting.Builder()
            .name("liquid-check")
            .description("Check if the player is in liquid.")
            .defaultTo(false)
            .addTo(coreGroup);
    private final BoolSetting restoreDelta = new BoolSetting.Builder()
            .name("restore-delta")
            .description("Re-apply delta movement.")
            .defaultTo(false)
            .addTo(coreGroup);
    private final BoolSetting grimRotBypass = new BoolSetting.Builder()
            .name("grim-rotation")
            .description("Grim is the most bypassable AC, actually.")
            .defaultTo(true)
            .addTo(coreGroup);
    private final BoolSetting jitterBug = new BoolSetting.Builder()
            .name("jitter-bug")
            .description("Makes you into a jitter bug.")
            .defaultTo(false)
            .addTo(coreGroup);
    private final FloatSetting idleRate = new FloatSetting.Builder()
            .name("idle-rate")
            .description("Elevation while idling.")
            .defaultTo(0)
            .range(-1, 1)
            .addTo(coreGroup);
    private final KeybindingSetting controlKey = new KeybindingSetting.Builder()
            .name("control-key")
            .description("The key to accelerate or stop depending on further configuration.")
            .defaultTo(new ImInt(KeyUtils.W))
            .addTo(coreGroup);
    private final BoolSetting invertControlKey = new BoolSetting.Builder()
            .name("invert-key")
            .description("If enabled makes the key stop you, off accelerates.")
            .defaultTo(false)
            .addTo(coreGroup);

    public ElytraPause() {
        super(Categories.MOVEMENT, "elytra-pause", "Allows you to pause flight with rockets.");
    }

    // Micro Optimization
    private final double[] previousDelta = new double[3];
    private boolean hasSetDelta = false;
    private boolean controlEnabled = false;

    @EventListener(priority = 10000)
    private void onPlayerMove(OnMove.Player event) {
        assert MC.player != null;

        if (
                !(MC.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA)
                        || !MC.player.isFallFlying()
                        || (liquidCheck.get() && MC.player.isInLiquid())) {
            return;
        }

        if (shouldControl()) {
            if (!hasSetDelta) {
                previousDelta[0] = event.vec3.x;
                previousDelta[1] = event.vec3.y;
                previousDelta[2] = event.vec3.z;
                hasSetDelta = true;
            }

            if (grimRotBypass.get()) {
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(-179.9f, -89.9f, false, MC.player.horizontalCollision));
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(179.9f, 89.9f, false, MC.player.horizontalCollision));
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(-179.9f, 89.9f, false, MC.player.horizontalCollision));
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(179.9f, -89.9f, false, MC.player.horizontalCollision));
            }

            if (jitterBug.get()) {
                MathUtils.setY(event.vec3, idleRate.get());
            } else {
                MathUtils.set(event.vec3, 0, idleRate.get(), 0);

            }

            MC.player.setDeltaMovement(0, idleRate.get(), 0);
            controlEnabled = true;
        } else if (controlEnabled) {
            if (hasSetDelta && restoreDelta.get()) {
                hasSetDelta = false;
                MathUtils.set(event.vec3, previousDelta[0], previousDelta[1], previousDelta[2]);
                MC.player.setDeltaMovement(previousDelta[0], previousDelta[1], previousDelta[2]);
            }

            controlEnabled = false;
        }
    }

    public boolean shouldControl() {
        return invertControlKey.get() == ImGui.isKeyDown(controlKey.value().get());
    }
}
