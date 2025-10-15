package monster.psyop.client.framework.modules.settings.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;

public class BlockEntityListSetting extends ObjectListSetting<BlockEntityListSetting, BlockEntityType<?>> {
    private final ArrayList<BlockEntityType<?>> suggestions;

    public BlockEntityListSetting(ObjectListSetting.Builder<BlockEntityListSetting, BlockEntityType<?>> builder) {
        super(builder);
        this.suggestions = new ArrayList<>(BuiltInRegistries.BLOCK_ENTITY_TYPE.stream().filter(filter).toList());
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

    public static class Builder extends ObjectListSetting.Builder<BlockEntityListSetting, BlockEntityType<?>> {
        @Override
        public BlockEntityListSetting build() {
            check();
            return new BlockEntityListSetting(this);
        }
    }
}
