package monster.psyop.client.utility;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

import static monster.psyop.client.Psyop.MC;

/**
 * The type Inv utils.
 */
public class InventoryUtils {
    public static final Predicate<ItemStack> IS_BLOCK =
            (itemStack) ->
                    BuiltInRegistries.BLOCK.containsKey(
                            ResourceLocation.parse("minecraft:" + getKey(itemStack.getItem())));

    public static String getKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    /**
     * Is wool boolean.
     *
     * @param item the item
     * @return the boolean
     */
    // Bed Utility
    public static boolean isWool(Item item) {
        return getKey(item).endsWith("_wool");
    }

    /**
     * Is plank boolean.
     *
     * @param item the item
     * @return the boolean
     */
    public static boolean isPlank(Item item) {
        return getKey(item).endsWith("_planks");
    }

    /**
     * Is bed boolean.
     *
     * @param item the item
     * @return the boolean
     */
    public static boolean isBed(Item item) {
        return getKey(item).endsWith("_bed");
    }

    /**
     * Is sword boolean.
     *
     * @param item the item
     * @return the boolean
     */
    public static boolean isSword(Item item) {
        return getKey(item).endsWith("_sword");
    }

    /**
     * Sync hand.
     *
     * @param i the
     */
    public static void syncHand(int i) {
        assert MC.player != null;
        PacketUtils.send(new ServerboundSetCarriedItemPacket(i));
    }

    /**
     * Sync hand.
     */
    public static void syncHand() {
        assert MC.player != null;
        syncHand(MC.player.getInventory().getSelectedSlot());
    }

    public static void swapSlot(int i) {
        assert MC.player != null;
        MC.player.getInventory().setSelectedSlot(i);
        MC.player.connection.send(new ServerboundSetCarriedItemPacket(i));
    }

    public static void dropSlot(AbstractContainerMenu container, int i, boolean stack) {
        if (MC.player == null || MC.gameMode == null || i > MC.player.getInventory().getContainerSize() + 16) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(container.containerId, i, stack ? 1 : 0, ClickType.THROW, MC.player);
    }

    public static int findEmptySlotInHotbar(int i) {
        if (MC.player != null) {
            for (var ref =
                 new Object() {
                     int i = 0;
                 };
                 ref.i < 9;
                 ref.i++) {
                if (MC.player.getInventory().getItem(getHotbarOffset() + ref.i).isEmpty()) {
                    return ref.i;
                }
            }
        }
        return i;
    }

    public static int getInventoryOffset() {
        assert MC.player != null;
        return MC.player.containerMenu.slots.size() == 46
                ? MC.player.containerMenu instanceof CraftingMenu ? 10 : 9
                : MC.player.containerMenu.slots.size() - 36;
    }

    public static int getHotbarOffset() {
        return getInventoryOffset() + 27;
    }

    public static void swapToHotbar(int slot, int hot) {
        if (MC.player == null || MC.gameMode == null) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(MC.player.containerMenu.containerId, slot, hot, ClickType.SWAP, MC.player);
    }

    public static void placeItem(int slot) {
        if (MC.player == null || MC.gameMode == null) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(MC.player.containerMenu.containerId, slot, 1, ClickType.PICKUP, MC.player);
    }

    public static void pickup(int slot) {
        if (MC.player == null || MC.gameMode == null) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(MC.player.containerMenu.containerId, slot, 0, ClickType.PICKUP, MC.player);
    }

    public static void quickMove(int slot) {
        if (MC.player == null || MC.gameMode == null) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(MC.player.containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, MC.player);
    }

    @FunctionalInterface
    public interface StackCheck {
        boolean run(ItemStack stack, int slot);
    }

    public static boolean isHotbarSlot(int slot) {
        if (slot < 0) return false;
        if (slot < 9) {
            return true;
        }

        return slot > getHotbarOffset();
    }

    /**
     * Find the slot of items.
     *
     * @param items A list of items to find.
     * @return The inventory slot
     */
    public static int findAnySlot(Item... items) {
        if (MC.player == null) {
            return -1;
        }

        List<Item> itemList = List.of(items);
        int slot = 0;

        for (ItemStack stack : MC.player.containerMenu.getItems()) {
            if (itemList.contains(stack.getItem())) {
                return slot;
            }

            slot++;
        }

        return -1;
    }

    public static int findAnySlot(List<Item> itemList) {
        if (MC.player == null) {
            return -1;
        }

        int slot = 0;

        for (ItemStack stack : MC.player.containerMenu.getItems()) {
            if (itemList.contains(stack.getItem())) {
                return slot;
            }

            slot++;
        }

        return -1;
    }

    /**
     * Find the slot of items.
     *
     * @param predicate Filter by item stack.
     * @return The inventory slot
     */
    public static int findAnySlot(Predicate<ItemStack> predicate) {
        if (MC.player == null) {
            return -1;
        }

        int slot = 0;

        for (ItemStack stack : MC.player.containerMenu.getItems()) {
            if (predicate.test(stack)) {
                return slot;
            }

            slot++;
        }

        return -1;
    }

    public static int findMatchingSlot(StackCheck check) {
        int slot = 0;
        assert MC.player != null;
        for (ItemStack stack : MC.player.containerMenu.getItems()) {
            if (check.run(stack, slot)) {
                return slot;
            }

            slot++;
        }

        return -1;
    }

    public static void dropSlot(int i, boolean stack) {
        if (MC.player == null || MC.gameMode == null) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(MC.player.containerMenu.containerId, i, stack ? 1 : 0, ClickType.THROW, MC.player);
    }

    /**
     * Gets offhand.
     *
     * @return the offhand
     */
    public static Item getOffhand() {
        assert MC.player != null;
        return MC.player.getOffhandItem().getItem();
    }

    /**
     * Gets main hand.
     *
     * @return the main hand
     */
    public static Item getMainHand() {
        assert MC.player != null;
        return MC.player.getInventory().getSelectedItem().getItem();
    }
}
