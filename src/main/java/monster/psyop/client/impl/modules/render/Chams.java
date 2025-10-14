package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

public class Chams extends Module {
    public GroupedSettings entityGroup = addGroup(new GroupedSettings("entities", "Modifies how entities are rendered in the world."));
    public EntityColorListSetting glowEntities = new EntityColorListSetting.Builder()
            .name("glow")
            .defaultTo(List.of(EntityType.PLAYER))
            .addTo(entityGroup);
    public ColorSetting entityGlowColor = new ColorSetting.Builder()
            .name("glow-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.6f})
            .addTo(entityGroup);
    public BoolSetting toggleGlow = new BoolSetting.Builder()
            .name("toggle-glow")
            .description("Toggles glow off.")
            .defaultTo(true)
            .addTo(entityGroup);
    public EntityListSetting walls = new EntityListSetting.Builder()
            .name("walls")
            .defaultTo(List.of(EntityType.PLAYER))
            .addTo(entityGroup);
    public GroupedSettings blockEntityGroup = addGroup(new GroupedSettings("block-entities", "Modifies how block entities are rendered in the world."));
    public BlockEntityColorListSetting glowBlockEntities = new BlockEntityColorListSetting.Builder()
            .name("glow")
            .defaultTo(List.of(BlockEntityType.CHEST, BlockEntityType.BANNER, BlockEntityType.TRAPPED_CHEST, BlockEntityType.SHULKER_BOX))
            .addTo(blockEntityGroup);
    public ColorSetting blockEntityGlowColor = new ColorSetting.Builder()
            .name("glow-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.6f})
            .addTo(blockEntityGroup);
    public BoolSetting alwaysRenderBlockEntities = new BoolSetting.Builder()
            .name("always-render")
            .description("Always makes block entities visible, enabled by default if chams is on.")
            .defaultTo(true)
            .addTo(blockEntityGroup);


    public Chams() {
        super(Categories.RENDER, "chams", "Modifies how entities/block entities render.");
    }
}
