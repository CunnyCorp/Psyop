package monster.psyop.client.utility;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.HashMap;

public class McDataCache {
    protected static HashMap<Item, MapColor> ITEM_TO_COLOR = new HashMap<>();
    protected static ArrayList<Integer> MAP_COLOR_IDS = new ArrayList<>();
    protected static HashMap<String, Item> NAME_TO_ITEM = new HashMap<>();

    public static void init() {
        refresh();
    }

    public static MapColor getColor(ItemStack stack) {
        return getColor(stack.getItem());
    }

    public static MapColor getColor(Item item) {

        return ITEM_TO_COLOR.getOrDefault(item, MapColor.NONE);
    }

    public static Item getItem(String string) {
        return NAME_TO_ITEM.getOrDefault(
                string.toLowerCase().replaceAll("[\\p{Zs}-_+]+", ""), Items.AIR);
    }

    public static void refresh() {
        Item.BY_BLOCK.forEach(
                (block, item) -> {
                    ITEM_TO_COLOR.put(item, block.defaultMapColor());

                    if (!MAP_COLOR_IDS.contains(block.defaultMapColor().id))
                        MAP_COLOR_IDS.add(block.defaultMapColor().id);
                });

        BuiltInRegistries.ITEM.forEach(
                item -> NAME_TO_ITEM.putIfAbsent(
                        item.getName().getString().toLowerCase().replaceAll("[\\p{Zs}-_+]+", ""), item));
    }

    public static void processLine(String str) {
        if (!str.contains(":")) {
            return;
        }

        String[] split = str.split(":");

        if (split.length > 2) {
            return;
        }

        String p1 = split[0].toLowerCase().replaceAll("[\\p{Zs}-_+]+", "");

        MapColor p2 = MapColor.byId(Integer.parseInt(split[1]));

        if (NAME_TO_ITEM.containsKey(p1) && p2 instanceof MapColor) {
            ITEM_TO_COLOR.put(getItem(p1), p2);
        }
    }
}
