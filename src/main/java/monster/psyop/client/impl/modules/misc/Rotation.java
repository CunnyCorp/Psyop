package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class Rotation extends Module {
    public GroupedSettings bypassGroup = addGroup(new GroupedSettings("bypassing", "Settings for bypasses."));
    public final BoolSetting bypass =
            new BoolSetting.Builder()
                    .name("bypass")
                    .description("Bypass server rotations.")
                    .defaultTo(true)
                    .addTo(bypassGroup);
    public final BoolSetting bypassUpdate =
            new BoolSetting.Builder()
                    .name("update")
                    .description("Inform the server of your rotations.")
                    .defaultTo(true)
                    .addTo(bypassGroup);

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
}
