package monster.psyop.client.impl.modules.combat;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Items;

public class AttackBurst extends Module {
    public final IntSetting rate =
            new IntSetting.Builder()
                    .name("rate")
                    .description("How many additional packets to send.")
                    .defaultTo(7)
                    .range(1, 15)
                    .addTo(coreGroup);
    public final GroupedSettings maceGroup = addGroup(new GroupedSettings("maces", "Various options for mace shenanigans"));
    public final BoolSetting maceOnly =
            new BoolSetting.Builder()
                    .name("mace-only")
                    .description("Only bursts with maces.")
                    .defaultTo(false)
                    .addTo(maceGroup);
    public final BoolSetting onlySmash =
            new BoolSetting.Builder()
                    .name("only-smash")
                    .description("Prevents bursting without smash.")
                    .defaultTo(false)
                    .addTo(maceGroup);
    public final BoolSetting onlySmashStrict =
            new BoolSetting.Builder()
                    .name("only-smash-strict")
                    .description("Prevents attacking at all without smash.")
                    .defaultTo(false)
                    .addTo(maceGroup);
    public final BoolSetting attemptGrimSpoof =
            new BoolSetting.Builder()
                    .name("attempt-grim-spoof")
                    .description("Tries to exploit grim.")
                    .defaultTo(false)
                    .addTo(maceGroup);


    public AttackBurst() {
        super(Categories.COMBAT, "attack-burst", "Sends additional attack packets.");
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundInteractPacket packet) {
            if (maceOnly.get()) {
                assert MC.player != null;
                if (MC.player.getMainHandItem().getItem() == Items.MACE) {
                    if ((onlySmash.get() || onlySmashStrict.get()) && MC.player.fallDistance <= 1.5f) {
                        if (onlySmashStrict.get()) {
                            event.cancel();
                        }
                        return;
                    }

                    if (MC.player.fallDistance > 1.5f) {
                        if (attemptGrimSpoof.get()) {
                            PacketUtils.send(new ServerboundMovePlayerPacket.StatusOnly(true, MC.player.horizontalCollision));
                        }
                    }
                } else {
                    return;
                }
            }

            if (packet.action.getType() == ServerboundInteractPacket.ActionType.ATTACK) {
                for (int i = 0; i < rate.get(); i++) {
                    PacketUtils.send(packet);
                }
            }
        }
    }
}
