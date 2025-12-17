package monster.psyop.client.impl.modules.render;

import imgui.type.ImString;
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
    public BoolSetting walls = new BoolSetting.Builder()
            .name("walls")
            .defaultTo(false)
            .addTo(entityGroup);
    public ProvidedStringSetting layerOneMode = new ProvidedStringSetting.Builder()
            .suggestions(List.of(new ImString("Translucent"), new ImString("Quads"), new ImString("Outline"), new ImString("None")))
            .name("layer-1-mode")
            .defaultTo(new ImString("Outline"))
            .addTo(entityGroup);
    public ColorSetting layerOneColor = new ColorSetting.Builder()
            .name("layer-1-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.8f})
            .addTo(entityGroup);
    public ColorSetting layerOneFriendColor = new ColorSetting.Builder()
            .name("layer-1-friend-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.8f})
            .addTo(entityGroup);
    public ProvidedStringSetting layerTwoMode = new ProvidedStringSetting.Builder()
            .suggestions(List.of(new ImString("Translucent"), new ImString("Quads"), new ImString("Outline"), new ImString("None")))
            .name("layer-2-mode")
            .defaultTo(new ImString("Quads"))
            .addTo(entityGroup);
    public ColorSetting layerTwoColor = new ColorSetting.Builder()
            .name("layer-2-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.6f})
            .addTo(entityGroup);
    public ColorSetting layerTwoFriendColor = new ColorSetting.Builder()
            .name("layer-2-friend-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.6f})
            .addTo(entityGroup);
    public ProvidedStringSetting layerThreeMode = new ProvidedStringSetting.Builder()
            .suggestions(List.of(new ImString("Translucent"), new ImString("Quads"), new ImString("Outline"), new ImString("None")))
            .name("layer-3-mode")
            .defaultTo(new ImString("Translucent"))
            .addTo(entityGroup);
    public ColorSetting layerThreeColor = new ColorSetting.Builder()
            .name("layer-3-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.4f})
            .addTo(entityGroup);
    public ColorSetting layerThreeFriendColor = new ColorSetting.Builder()
            .name("layer-3-friend-color")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 0.4f})
            .addTo(entityGroup);
    public BoolSetting showCapes = new BoolSetting.Builder()
            .name("show-capes")
            .defaultTo(true)
            .addTo(entityGroup);
    public GroupedSettings blockEntityGroup = addGroup(new GroupedSettings("block-entities", "Modifies how block entities are rendered in the world."));
    public BlockEntityColorListSetting blockEntities = new BlockEntityColorListSetting.Builder()
            .name("block-entities")
            .defaultTo(List.of(BlockEntityType.CHEST, BlockEntityType.BANNER, BlockEntityType.TRAPPED_CHEST, BlockEntityType.SHULKER_BOX))
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
