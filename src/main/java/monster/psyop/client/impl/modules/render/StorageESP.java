package monster.psyop.client.impl.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BlockEntityColorListSetting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.game.OnRender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class StorageESP extends Module {
    public GroupedSettings renderingGroup = addGroup(new GroupedSettings("Rendering", "Rendering options."));
    public BlockEntityColorListSetting storage = new BlockEntityColorListSetting.Builder()
            .name("storage")
            .defaultTo(new ArrayList<>(List.of(BlockEntityType.CHEST, BlockEntityType.TRAPPED_CHEST, BlockEntityType.SHULKER_BOX, BlockEntityType.ENDER_CHEST)))
            .addTo(renderingGroup);
    public IntSetting cacheDelay = new IntSetting.Builder()
            .name("cache-delay")
            .defaultTo(10)
            .range(0, 1000)
            .addTo(renderingGroup);
    public FloatSetting fillOpacity = new FloatSetting.Builder()
            .name("fill-opacity")
            .defaultTo(0.25f)
            .range(0.0f, 1.0f)
            .addTo(renderingGroup);
    public FloatSetting subtraction = new FloatSetting.Builder()
            .name("subtraction")
            .defaultTo(0.05f)
            .range(0.0f, 1.0f)
            .addTo(renderingGroup);
    public BoolSetting pulse = new BoolSetting.Builder()
            .name("pulse")
            .defaultTo(false)
            .addTo(renderingGroup);
    public FloatSetting pulseRate = new FloatSetting.Builder()
            .name("pulse-rate")
            .defaultTo(0.005f)
            .range(0.0f, 0.2f)
            .addTo(renderingGroup);
    public FloatSetting maxExpansion = new FloatSetting.Builder()
            .name("max-expansion")
            .defaultTo(0.08f)
            .range(0.0f, 1.0f)
            .addTo(renderingGroup);

    public int timer = 0;
    public List<RenderBox> boundingBoxes = new ArrayList<>();
    public List<BlockPos> skipped = new ArrayList<>();
    public float pulseStage = 0.0f;
    public boolean pulsingOut = true;

    public StorageESP() {
        super(Categories.RENDER, "storage-esp", "A full rebase of StorageEsp");
    }

    @Override
    public void update() {
        if (pulse.get()) {
            if (pulseStage >= maxExpansion.get()) {
                pulsingOut = false;
            } else if (!pulsingOut && pulseStage <= -maxExpansion.get()) {
                pulsingOut = true;
            }

            if (pulsingOut) {
                pulseStage += pulseRate.get();
            } else {
                pulseStage -= pulseRate.get();
            }
        }

        if (timer >= cacheDelay.get()) {
            timer = 0;

            boundingBoxes.clear();
            skipped.clear();

            for (int x = -(MC.options.renderDistance().get() + 2); x <= MC.options.renderDistance().get() + 2; x++) {
                for (int z = -(MC.options.renderDistance().get() + 2); z <= MC.options.renderDistance().get() + 2; z++) {
                    if (MC.level.hasChunk(MC.player.chunkPosition().x + x, MC.player.chunkPosition().z + z)) {
                        ChunkAccess chunk = MC.level.getChunk(MC.player.chunkPosition().x + x, MC.player.chunkPosition().z + z);

                        for (BlockPos entPos : chunk.getBlockEntitiesPos()) {
                            BlockEntity blockEntity = chunk.getBlockEntity(entPos);

                            if (blockEntity == null || skipped.contains(entPos)) {
                                continue;
                            }

                            float[] color = storage.colorMap.getOrDefault(blockEntity.getType(), new float[]{1.0f, 0.6f, 0.0f, 1.0f});
                            color[3] = fillOpacity.get();

                            if (storage.value().contains(blockEntity.getType())) {
                                if (isDoubleChest(entPos)) {
                                    Direction direction = ChestBlock.getConnectedDirection(MC.level.getBlockState(entPos));
                                    BlockPos side = entPos.relative(direction);

                                    // We don't want to deal with this shit.
                                    double xOffset = side.getX() - entPos.getX();
                                    double zOffset = side.getZ() - entPos.getZ();

                                    if (zOffset == -1.0 || xOffset == -1.0) {
                                        skipped.add(entPos);
                                        continue;
                                    }

                                    double x0 = (entPos.getX() + 1 + xOffset + pulseStage) - subtraction.get();
                                    double y0 = (entPos.getY() + 1 + pulseStage) - subtraction.get();
                                    double z0 = (entPos.getZ() + 1 + zOffset + pulseStage) - subtraction.get();


                                    boundingBoxes.add(new RenderBox(new AABB((entPos.getX() - pulseStage) + subtraction.get(), (entPos.getY() - pulseStage) + subtraction.get(), (entPos.getZ() - pulseStage) + subtraction.get(), x0, y0, z0), color, storage.colorMap.getOrDefault(blockEntity.getType(), new float[]{1.0f, 0.6f, 0.0f, 1.0f})));
                                    skipped.add(side);
                                } else {
                                    double x0 = (entPos.getX() + 1 + pulseStage) - subtraction.get();
                                    double y0 = (entPos.getY() + 1 + pulseStage) - subtraction.get();
                                    double z0 = (entPos.getZ() + 1 + pulseStage) - subtraction.get();
                                    boundingBoxes.add(new RenderBox(new AABB((entPos.getX() - pulseStage) + subtraction.get(), (entPos.getY() - pulseStage) + subtraction.get(), (entPos.getZ() - pulseStage) + subtraction.get(), x0, y0, z0), color, storage.colorMap.getOrDefault(blockEntity.getType(), new float[]{1.0f, 0.6f, 0.0f, 1.0f})));
                                }
                            }
                        }
                    }
                }
            }
        }

        timer++;
    }

    @EventListener
    public void onRender(OnRender event) {
        PoseStack.Pose pose = event.poseStack.last();

        for (RenderBox bb : boundingBoxes) {
            Render3DUtil.drawBoxInner(event.quads, pose, bb.pos, MC.gameRenderer.getMainCamera().getPosition(), bb.faces);
            Render3DUtil.drawBoxOutline(event.lines, pose, bb.pos, MC.gameRenderer.getMainCamera().getPosition(), bb.edges);
        }
    }

    public boolean isDoubleChest(BlockPos blockPos) {
        boolean passesDist =
                blockPos.getX() > 20000000 || blockPos.getX() < -20000000 ||
                blockPos.getZ() > 20000000 || blockPos.getZ() < -20000000;

        // Patch for a bug where containers after a certain distance are never considered single, how mojang? how?
        if (passesDist) {
            return false;
        }

        return MC.level != null && MC.level.getBlockState(blockPos).getValueOrElse(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE) != ChestType.SINGLE;
    }

    public record RenderBox(AABB pos, float[] faces, float[] edges) {

    }
}
