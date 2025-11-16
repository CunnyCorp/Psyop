package monster.psyop.client.impl.modules.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.modules.hud.HUD;
import monster.psyop.client.impl.modules.player.AutoTool;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.RotationUtils;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Nuker extends Module {
    public final GroupedSettings generalGroup = addGroup(new GroupedSettings("general", "Core settings"));
    public final GroupedSettings renderingGroup = addGroup(new GroupedSettings("rendering", "Rendering settings"));

    public final BoolSetting ignoreWhitelist = new BoolSetting.Builder()
            .name("ingore-whitelist")
            .description("Whether or not to ignore the whitelist.")
            .defaultTo(true)
            .addTo(generalGroup);
    public final BlockListSetting blocks = new BlockListSetting.Builder()
            .name("blocks")
            .description("A list of blocks to mine.")
            .defaultTo(List.of(Blocks.EMERALD_BLOCK, Blocks.END_STONE))
            .addTo(generalGroup);
    public final FloatSetting maxDistance = new FloatSetting.Builder()
            .name("max-distance")
            .description("Max mining distance.")
            .defaultTo(3.75f)
            .range(2f, 5.0f)
            .addTo(generalGroup);
    public final IntSetting yScan = new IntSetting.Builder()
            .name("y-scan")
            .description("How far to check for Y.")
            .defaultTo(2)
            .range(0, 5)
            .addTo(generalGroup);
    public final IntSetting blocksPerTick = new IntSetting.Builder()
            .name("blocks-per-tick")
            .description("How many blocks to mine per tick.")
            .defaultTo(12)
            .range(1, 32)
            .addTo(generalGroup);

    public BoolSetting radius = new BoolSetting.Builder()
            .name("radius")
            .description("Whether or not to render a sphere the size of your reach.")
            .defaultTo(false)
            .addTo(renderingGroup);
    public ColorSetting radiusColor = new ColorSetting.Builder()
            .name("radius-color")
            .defaultTo(new float[]{0.0f, 0.0f, 1.0f, 0.15f})
            .addTo(renderingGroup);
    public BoolSetting attemptBreak = new BoolSetting.Builder()
            .name("attempt-break")
            .defaultTo(true)
            .addTo(renderingGroup);
    public ColorSetting breakColor = new ColorSetting.Builder()
            .name("break-color")
            .defaultTo(new float[]{0.0f, 1.0f, 0.0f, 0.4f})
            .addTo(renderingGroup);
    public IntSetting expireTime = new IntSetting.Builder()
            .name("expire-time")
            .defaultTo(2500)
            .range(1000, 10000)
            .addTo(renderingGroup);

    public List<BrokenBlock> brokenBlocks = new ArrayList<>();

    public Nuker() {
        super(Categories.WORLD, "nuker", "Very fast block breaking brrr.");
    }

    @Override
    public void update() {
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

            Psyop.MODULES.get(AutoTool.class).findAndSwitch(mutableBlockPos);

            brokenBlocks.removeIf(brokenBlock -> brokenBlock.pos.equals(mutableBlockPos));
            brokenBlocks.add(new BrokenBlock(mutableBlockPos.immutable(), System.currentTimeMillis(), expireTime.get()));
            PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, mutableBlockPos, getDirection(mutableBlockPos)));
            PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, mutableBlockPos.setY(mutableBlockPos.getY() + 1337), getDirection(mutableBlockPos.setY(mutableBlockPos.getY()))));
            PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, mutableBlockPos.setY(vec[1]), getDirection(mutableBlockPos)));
        }

        List<BrokenBlock> toRemove = new ArrayList<>();

        for (BrokenBlock block : brokenBlocks) {
            if (block.isExpired()) {
                toRemove.add(block);
            }
        }

        brokenBlocks.removeAll(toRemove);
    }

    @EventListener
    public void onRender(OnRender event) {
        PoseStack.Pose pose = event.poseStack.last();

        RenderSystem.lineWidth(10f);

        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        if (radius.get()) {
            float cx = (float) (MC.player.position().x - camX);
            float cy = (float) (MC.player.position().y - camY) + MC.player.getEyeHeight() / 2;
            float cz = (float) (MC.player.position().z - camZ);

            Render3DUtil.drawSphereFaces(event.quads, pose, cx, cy, cz, (float) MC.player.blockInteractionRange(), 24, radiusColor.get()[0], radiusColor.get()[1], radiusColor.get()[2], radiusColor.get()[3]);
        }

        if (breakColor.get()[3] > 0.0f && attemptBreak.get()) {
            for (BrokenBlock block : brokenBlocks) {
                if (block.isExpired()) continue;
                Render3DUtil.drawBlockInner(event.quads, pose, block.pos, cam, 0.0f, breakColor.get()[0], breakColor.get()[1], breakColor.get()[2], Math.min(0.0f, breakColor.get()[3] * block.getAlpha()));
                Render3DUtil.drawBlockOutline(event.lines, pose, block.pos, cam, 0.0f, breakColor.get()[0], breakColor.get()[1], breakColor.get()[2], breakColor.get()[3]);
            }
        }
    }

    // qnx told me to add it - https://github.com/MeteorDevelopment/meteor-client/pull/5840#issuecomment-3515069710
    public static Direction getDirection(BlockPos pos) {
        double eyePos = MC.player.getY() + MC.player.getEyeHeight(MC.player.getPose());
        VoxelShape outline = MC.level.getBlockState(pos).getCollisionShape(MC.level, pos);

        if (eyePos > pos.getY() + outline.max(Direction.Axis.Y) && MC.level.getBlockState(pos.offset(0, 1, 0)).canBeReplaced()) {
            return Direction.UP;
        } else if (eyePos < pos.getY() + outline.min(Direction.Axis.Y) && MC.level.getBlockState(pos.offset(0, -1, 0)).canBeReplaced()) {
            return Direction.DOWN;
        } else {
            BlockPos difference = pos.subtract(MC.player.blockPosition());

            if (Math.abs(difference.getX()) > Math.abs(difference.getZ())) {
                return difference.getX() > 0 ? Direction.WEST : Direction.EAST;
            } else {
                return difference.getZ() > 0 ? Direction.NORTH : Direction.SOUTH;
            }
        }
    }

    public record BrokenBlock(BlockPos pos, long keepAliveMS, long expireTimeMS) {
        public boolean isExpired() {
            return System.currentTimeMillis() - keepAliveMS >= expireTimeMS;
        }

        public float getAlpha() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - keepAliveMS;

            if (elapsedTime >= expireTimeMS || expireTimeMS <= 0) {
                return 0.0f;
            }

            float remainingTime = expireTimeMS - elapsedTime;
            float percentage = remainingTime / (float) expireTimeMS;

            return Math.max(0.0f, percentage);
        }
    }
}

