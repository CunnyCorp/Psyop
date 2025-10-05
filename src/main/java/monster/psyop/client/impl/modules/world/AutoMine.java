package monster.psyop.client.impl.modules.world;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BlockListSetting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoMine extends Module {
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
    public final BoolSetting nuker =
            new BoolSetting.Builder()
                    .name("nuker")
                    .description("Automatically aim at mineable blocks.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final FloatSetting rotationSpeed =
            new FloatSetting.Builder()
                    .name("rotation-speed")
                    .description("Speed to rotate towards entities at.")
                    .defaultTo(0.32f)
                    .range(0.5f, 1.1f)
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


    private BlockPos lastAttackedPos = BlockPos.ZERO;

    public AutoMine() {
        super(Categories.WORLD, "auto-mine", "Automatically mines blocks you look at.");
    }

    @EventListener
    public void onTick(OnTick.Post ignored) {
        if (isLookingAtValidBlock()) {
            if (((BlockHitResult) MC.hitResult).getBlockPos().equals(lastAttackedPos)) {
                MC.continueAttack(true);
            } else {
                MC.startAttack();
                lastAttackedPos = ((BlockHitResult) MC.hitResult).getBlockPos();
            }
        } else if (nuker.get()) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            List<int[]> blockVecs = new ArrayList<>();
            for (int y = (MC.player.getAbilities().flying ? -yScan.get() : 0); y <= yScan.get(); y++) {
                blockVecs.addAll(BlockUtils.findNearBlocksByRadius(MC.player.blockPosition().offset(0, y, 0).mutable(), 5, (vecPos) -> {
                    mutableBlockPos.set(vecPos[0], vecPos[1], vecPos[2]);

                    if (!ignoreWhitelist.get() && !blocks.value().contains(BlockUtils.getState(mutableBlockPos).getBlock())) {
                        return false;
                    }

                    if (!BlockUtils.isExposedToAir(mutableBlockPos)) {
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

            int[] firstVec = blockVecs.get(0);

            float playerPitch = Mth.clamp(MC.player.getXRot(), -90.0f, 90.0f);
            float playerYaw = Mth.wrapDegrees(MC.player.getYRot());

            mutableBlockPos.set(firstVec[0], firstVec[1], firstVec[2]);

            float pitch = RotationUtils.getPitch(mutableBlockPos);
            float yaw = RotationUtils.getYaw(mutableBlockPos);

            playerPitch = Mth.rotLerp(rotationSpeed.get(), playerPitch, pitch);
            playerYaw = Mth.rotLerp(rotationSpeed.get(), playerYaw, yaw);

            RotationUtils.rotate(playerPitch, playerYaw);
        }
    }

    public boolean isLookingAtValidBlock() {
        if (MC.player == null
                || MC.hitResult == null
                || MC.hitResult.getType() != HitResult.Type.BLOCK
                || MC.level == null) {
            return false;
        }

        BlockPos blockPos = ((BlockHitResult) MC.hitResult).getBlockPos();

        BlockState state = MC.level.getBlockState(blockPos);

        if (state.getDestroySpeed(MC.level, blockPos) == -1.0f) {
            return false;
        }

        if (BlockUtils.isLiquid(blockPos)) {
            return false;
        }

        return ignoreWhitelist.get() || blocks.value().contains(state.getBlock());
    }
}

