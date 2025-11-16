package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;

public class Rotation extends Module {
    public GroupedSettings bypassGroup = addGroup(new GroupedSettings("bypassing", "Settings for bypasses."));
    public final BoolSetting bypass = new BoolSetting.Builder()
            .name("bypass")
            .description("Bypass server rotations.")
            .defaultTo(true)
            .addTo(bypassGroup);
    public final BoolSetting bypassUpdate = new BoolSetting.Builder()
            .name("update")
            .description("Inform the server of your rotations.")
            .defaultTo(true)
            .addTo(bypassGroup);
    public GroupedSettings spoofGroup = addGroup(new GroupedSettings("spoof", "Spoof funny option"));
    public BoolSetting reverse = new BoolSetting.Builder()
            .name("reverse")
            .defaultTo(false)
            .addTo(spoofGroup);
    public BoolSetting headBang = new BoolSetting.Builder()
            .name("head-bang")
            .defaultTo(false)
            .addTo(spoofGroup);
    public BoolSetting random = new BoolSetting.Builder()
            .name("random")
            .defaultTo(false)
            .addTo(spoofGroup);
    public IntSetting randomAmount = new IntSetting.Builder()
            .name("random-amount")
            .defaultTo(3)
            .range(1, 10)
            .addTo(spoofGroup);

    public boolean isUp = false;

    public Rotation() {
        super(Categories.MISC, "rotations", "Allows you to modify how you rotate.");
    }

    @EventListener
    public void onPacketSend(OnPacket.Received event) {
        if (bypass.value().get() && event.packet() instanceof ClientboundPlayerLookAtPacket) {
            event.cancel();
            if (bypassUpdate.value().get()) {
                assert MC.player != null;
                PacketUtils.send(
                        new ServerboundMovePlayerPacket.Rot(
                                MC.player.getYRot(), MC.player.getXRot(), MC.player.onGround(), MC.player.horizontalCollision));
            }
        }
    }

    @Override
    public void update() {
        boolean updateFresh = false;

        if (reverse.get()) {
            PacketUtils.send(new ServerboundMovePlayerPacket.Rot(-MC.player.getYRot(), -MC.player.getXRot(), MC.player.onGround(), MC.player.horizontalCollision));
            updateFresh = true;
        }

        if (random.get()) {
            for (int i = 0; i < randomAmount.get(); i++) {
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(Mth.clamp(Psyop.RANDOM.nextFloat() * 180, -90, 90), Mth.wrapDegrees(Psyop.RANDOM.nextFloat() * (180 * 2)), MC.player.onGround(), MC.player.horizontalCollision));
            }
            updateFresh = true;
        }

        if (updateFresh) {
            PacketUtils.send(new ServerboundMovePlayerPacket.Rot(MC.player.getYRot(), MC.player.getXRot(), MC.player.onGround(), MC.player.horizontalCollision));
        }
    }

    @EventListener(priority = 999999999)
    public void onTickPre(OnTick.Pre event) {
        if (headBang.get()) {
            if (isUp) {
                PacketUtils.rotate(90f, MC.player.getYRot(), true);
            } else {
                PacketUtils.rotate(-90f, MC.player.getYRot(), true);
            }
        }
    }
}
