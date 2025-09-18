package monster.psyop.client.config.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import monster.psyop.client.utility.blocks.BlockUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.io.IOException;

public class BlockAdapter extends TypeAdapter<Block> {
    @Override
    public void write(JsonWriter out, Block value) throws IOException {
        out.value(BlockUtils.getKey(value));
    }

    @Override
    public Block read(JsonReader in) throws IOException {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:" + in.nextString())).get().value();
    }
}
