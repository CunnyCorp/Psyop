package monster.psyop.client.framework.modules.settings.types;

import monster.psyop.client.utility.InventoryUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;

public class ItemListSetting extends ObjectListSetting<ItemListSetting, Item> {
    private final ArrayList<Item> suggestions;

    public ItemListSetting(ObjectListSetting.Builder<ItemListSetting, Item> builder) {
        super(builder);
        this.suggestions = new ArrayList<>(BuiltInRegistries.ITEM.stream().filter(filter).toList());
    }

    @Override
    public ArrayList<Item> getSuggestions() {
        return suggestions;
    }

    @Override
    public Item parseItem(String v) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:" + v)).get().value();
    }

    @Override
    public String itemToString(Item v) {
        return InventoryUtils.getKey(v);
    }

    public static class Builder extends ObjectListSetting.Builder<ItemListSetting, Item> {
        @Override
        public ItemListSetting build() {
            check();
            return new ItemListSetting(this);
        }
    }
}
