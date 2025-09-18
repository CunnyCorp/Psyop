package monster.psyop.client.config.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import monster.psyop.client.utility.InventoryUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.io.IOException;

public class ItemAdapter extends TypeAdapter<Item> {
    @Override
    public void write(JsonWriter out, Item value) throws IOException {
        out.value(InventoryUtils.getKey(value));
    }

    @Override
    public Item read(JsonReader in) throws IOException {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:" + in.nextString())).get().value();
    }
}
