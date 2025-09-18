package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BlockListSetting;
import monster.psyop.client.framework.modules.settings.types.BlockPosSetting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImBlockPos;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class TestModule extends Module {
    public GroupedSettings testGroup =
            addGroup(new GroupedSettings("sex!", "UOOOHHHH EROTIC CHILD TUMMY !!! EROTIC !!!"));
    public BoolSetting sexSetting =
            new BoolSetting.Builder()
                    .name("sex")
                    .description("HAS SEX !")
                    .defaultTo(false)
                    .addTo(testGroup);
    public IntSetting numberino =
            new IntSetting.Builder()
                    .name("number")
                    .description("A setting probably")
                    .range(1, 20)
                    .defaultTo(6)
                    .addTo(testGroup);
    public BlockListSetting blocks =
            new BlockListSetting.Builder()
                    .name("blocks")
                    .description("OOOOOOOOOOOOO")
                    .defaultTo(List.of(Blocks.BEDROCK))
                    .filter(block -> block.getDescriptionId().contains("be"))
                    .addTo(testGroup);

    public BlockPosSetting blockPos =
            new BlockPosSetting.Builder()
                    .name("block-pos")
                    .description("OOOOOOOOOOOOO")
                    .defaultTo(new ImBlockPos())
                    .addTo(testGroup);

    public TestModule() {
        super(Categories.MISC, "testing", "A module designed for testing.");
    }

    @EventListener
    public void onPacketSend(OnPacket.Received event) {
        if (event.packet() instanceof ClientboundContainerSetSlotPacket packet) {
            if (packet.getSlot() < 0) event.cancel();
        }
    }

    @EventListener
    public void onPreTick(OnTick.Pre event) {
        if (MC.player == null) return;
        for (int i = 0; i < numberino.value().get(); i++) {
            PacketUtils.send(
                    new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
            PacketUtils.send(new ServerboundContainerClosePacket(-1));
            // PacketUtils.send(new ServerboundContainerClickPacket(mc.player.containerMenu.containerId,
            // mc.player.containerMenu.getStateId(), -999, 0, ClickType.QUICK_CRAFT, ItemStack.EMPTY, new
            // Int2ObjectOpenHashMap<>()));
        }
    }
}
