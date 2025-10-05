package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BlockListSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.utility.CollectionUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class SillyBot extends Module {
    public final BlockListSetting blocks =
            new BlockListSetting.Builder()
                    .name("blocks")
                    .description("A list of blocks to follow.")
                    .defaultTo(List.of(Blocks.EMERALD_BLOCK, Blocks.END_STONE))
                    .addTo(coreGroup);
    private final IntSetting radius =
            new IntSetting.Builder()
                    .name("radius")
                    .description("How far to scan for blocks.")
                    .defaultTo(32)
                    .range(6, 128)
                    .addTo(coreGroup);
    public final FloatSetting rotationSpeed =
            new FloatSetting.Builder()
                    .name("rotation-speed")
                    .description("Speed to rotate towards entities at.")
                    .defaultTo(0.32f)
                    .range(0.5f, 1.1f)
                    .addTo(coreGroup);
    public final FloatSetting goalDist =
            new FloatSetting.Builder()
                    .name("goal-dist")
                    .description("Speed to rotate towards entities at.")
                    .defaultTo(3f)
                    .range(0.5f, 6)
                    .addTo(coreGroup);


    private final BlockPos.MutableBlockPos nextGoal = new BlockPos.MutableBlockPos();

    public SillyBot() {
        super(Categories.MOVEMENT, "silly-bot", "Acts like a silly lil bot, bit chill.");
    }

    @Override
    public void update() {
        if ((nextGoal.getX() == 0 && nextGoal.getZ() == 0) || !MC.player.position().closerThan(nextGoal.getCenter(), radius.get() * 3)) {
            setNewGoal();
        }

        if (MC.player.position().with(Direction.Axis.Y, nextGoal.getY()).closerThan(nextGoal.getCenter(), goalDist.get())) {
            setNewGoal();
            MC.options.keyUp.setDown(false);
        } else {
            RotationUtils.smoothRotateTowards(RotationUtils.getYaw(nextGoal.getCenter()), rotationSpeed.get());
            MC.options.keyUp.setDown(true);
        }
    }

    private void setNewGoal() {
        List<int[]> nearbyBlocks = new ArrayList<>();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int i = -(radius.get() / 2); i <= (radius.get() / 2); i++) {
            nearbyBlocks.addAll(BlockUtils.findNearBlocksByRadius(MC.player.blockPosition().offset(0, i, 0).mutable(), radius.get(), (pos) -> {
                mutableBlockPos.set(pos[0], pos[1], pos[2]);

                return blocks.value().contains(MC.level.getBlockState(mutableBlockPos).getBlock());
            }));
        }

        int[] vec = CollectionUtils.random(nearbyBlocks);

        nextGoal.set(vec[0], vec[1], vec[2]);
    }
}
