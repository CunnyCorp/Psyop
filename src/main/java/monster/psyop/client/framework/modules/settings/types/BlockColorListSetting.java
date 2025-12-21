package monster.psyop.client.framework.modules.settings.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;

public class BlockColorListSetting extends ObjectColorListSetting<BlockColorListSetting, Block> {
    private final ArrayList<Block> suggestions;

    public BlockColorListSetting(ObjectColorListSetting.Builder<BlockColorListSetting, Block> builder) {
        super(builder);
        this.suggestions = new ArrayList<>(BuiltInRegistries.BLOCK.stream().filter(filter).toList());
        this.identifier = "entity";
    }

    @Override
    public ArrayList<Block> getSuggestions() {
        return suggestions;
    }

    @Override
    public Block parseItem(String v) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:" + v)).get().value();
    }

    @Override
    public String itemToString(Block v) {
        return BuiltInRegistries.BLOCK.getKey(v).getPath();
    }

    public static class Builder extends ObjectColorListSetting.Builder<BlockColorListSetting, Block> {
        @Override
        public BlockColorListSetting build() {
            check();
            return new BlockColorListSetting(this);
        }
    }
}
