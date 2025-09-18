package monster.psyop.client.framework.modules.settings.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;

public class BlockEntityColorListSetting extends ObjectColorListSetting<BlockEntityColorListSetting, BlockEntityType<?>> {
    private final ArrayList<BlockEntityType<?>> suggestions;

    public BlockEntityColorListSetting(ObjectColorListSetting.Builder<BlockEntityColorListSetting, BlockEntityType<?>> builder) {
        super(builder);
        this.suggestions = new ArrayList<>(BuiltInRegistries.BLOCK_ENTITY_TYPE.stream().filter(filter).toList());
        this.identifier = "block_entity";
    }

    @Override
    public ArrayList<BlockEntityType<?>> getSuggestions() {
        return suggestions;
    }

    @Override
    public BlockEntityType<?> parseItem(String v) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.parse("minecraft:" + v)).get().value();
    }

    @Override
    public String itemToString(BlockEntityType<?> v) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(v).getPath();
    }

    public static class Builder extends ObjectColorListSetting.Builder<BlockEntityColorListSetting, BlockEntityType<?>> {
        @Override
        public BlockEntityColorListSetting build() {
            check();
            return new BlockEntityColorListSetting(this);
        }
    }
}
