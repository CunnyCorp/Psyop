package monster.psyop.client.impl.modules.world;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BlockListSetting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Nuker extends Module {
    public final BoolSetting ignoreWhitelist =
            new BoolSetting.Builder()
                    .name("ingore-whitelist")
                    .description("Whether or not to ignore the whitelist.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final BlockListSetting blocks =
            new BlockListSetting.Builder()
                    .name("blocks")
                    .description("A list of blocks to mine.")
                    .defaultTo(List.of(Blocks.EMERALD_BLOCK, Blocks.END_STONE))
                    .addTo(coreGroup);
    public final FloatSetting maxDistance =
            new FloatSetting.Builder()
                    .name("max-distance")
                    .description("Max mining distance.")
                    .defaultTo(3.75f)
                    .range(2f, 5.0f)
                    .addTo(coreGroup);
    public final IntSetting yScan =
            new IntSetting.Builder()
                    .name("y-scan")
                    .description("How far to check for Y.")
                    .defaultTo(2)
                    .range(0, 5)
                    .addTo(coreGroup);
    public final IntSetting blocksPerTick =
            new IntSetting.Builder()
                    .name("blocks-per-tick")
                    .description("How many blocks to mine per tick.")
                    .defaultTo(12)
                    .range(1, 16)
                    .addTo(coreGroup);

    public Nuker() {
        super(Categories.WORLD, "nuker", "Very fast block breaking brrr.");
    }

    @EventListener
    public void onTick(OnTick.Post ignored) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            List<int[]> blockVecs = new ArrayList<>();
            for (int y = (MC.player.getAbilities().flying ? -yScan.get() : 0); y <= yScan.get(); y++) {
                blockVecs.addAll(BlockUtils.findNearBlocksByRadius(MC.player.blockPosition().offset(0, y, 0).mutable(), 5, (vecPos) -> {
                    mutableBlockPos.set(vecPos[0], vecPos[1], vecPos[2]);

                    if (!ignoreWhitelist.get() && !blocks.value().contains(BlockUtils.getState(mutableBlockPos).getBlock())) {
                        return false;
                    }

                    BlockState state = MC.level.getBlockState(mutableBlockPos);

                    if (state.getDestroySpeed(MC.level, mutableBlockPos) == -1.0f) {
                        return false;
                    }

                    if (BlockUtils.isLiquid(mutableBlockPos)) {
                        return false;
                    }

                    if (BlockUtils.isAir(mutableBlockPos)) {
                        return false;
                    }

                    return MC.player.getEyePosition().distanceTo(mutableBlockPos.getCenter()) <= maxDistance.get();
                }));
            }

            blockVecs.sort(Comparator.comparingDouble((value) -> {
                mutableBlockPos.set(value[0], value[1], value[2]);

                return Math.abs(RotationUtils.getYaw(mutableBlockPos) - MC.player.getYRot()) + Math.abs(RotationUtils.getPitch(mutableBlockPos) - MC.player.getXRot());
            }));

            for (int i = 0; i < blocksPerTick.get(); i++) {
                if (blockVecs.size() <= i) break;

                int[] vec = blockVecs.get(i);

                mutableBlockPos.set(vec[0], vec[1], vec[2]);

                if (MC.player.getEyePosition().distanceTo(mutableBlockPos.getCenter()) > maxDistance.get()) continue;

                PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, mutableBlockPos, Direction.UP));
                PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, mutableBlockPos.setY(mutableBlockPos.getY() + 1337), Direction.UP));
                PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, mutableBlockPos.setY(vec[1]), Direction.UP));
            }
    }
}

