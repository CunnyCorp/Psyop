package monster.psyop.client.impl.modules.combat;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.InventoryUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoElytraSwap extends Module {
    private final IntSetting range =
            new IntSetting.Builder()
                    .name("range")
                    .description("Distance to swap into chestplate.")
                    .defaultTo(8)
                    .range(1, 20)
                    .addTo(coreGroup);
    private final IntSetting hotbarSlot =
            new IntSetting.Builder()
                    .name("hotbar-slot")
                    .description("Preferred hotbar slot for swapped item (1-9).")
                    .defaultTo(6)
                    .range(1, 9)
                    .addTo(coreGroup);
    private final IntSetting delay =
            new IntSetting.Builder()
                    .name("delay")
                    .description("Delay in ticks between swap actions.")
                    .defaultTo(5)
                    .range(1, 20)
                    .addTo(coreGroup);

    private static final int CHEST_SLOT_INDEX = 6;
    private int tickCounter = 0;

    public AutoElytraSwap() {
        super(Categories.COMBAT, "auto-elytra-swap", "Swaps elytra and chestplate depending on nearby players.");
    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        if (MC.player == null || MC.level == null) return;

        if (tickCounter++ < delay.get()) return;
        tickCounter = 0;

        boolean enemyNearby = MC.level.players().stream()
                .anyMatch(p -> p != MC.player && MC.player.distanceTo(p) <= range.get());

        ItemStack chestSlot = MC.player.getItemBySlot(EquipmentSlot.CHEST);
        boolean chestIsElytra = chestSlot.getItem() == Items.ELYTRA;

        if (enemyNearby) {
            if (chestIsElytra) {
                int chestplateSlot = InventoryUtils.findAnySlot(stack ->
                        stack.getItem() == Items.NETHERITE_CHESTPLATE ||
                                stack.getItem() == Items.DIAMOND_CHESTPLATE ||
                                stack.getItem() == Items.IRON_CHESTPLATE);
                if (chestplateSlot != -1) {
                    InventoryUtils.pickup(chestplateSlot);
                    InventoryUtils.placeItem(CHEST_SLOT_INDEX);

                    int slotIndex = hotbarSlot.get() - 1;
                    int invSlot = InventoryUtils.getHotbarOffset() + slotIndex;
                    if (MC.player.getInventory().getItem(slotIndex).isEmpty()) {
                        InventoryUtils.placeItem(invSlot);
                    } else {
                        moveToAnyEmptySlot();
                    }
                }
            }
        } else {
            if (!chestIsElytra) {
                int elytraSlot = InventoryUtils.findAnySlot(stack -> stack.getItem() == Items.ELYTRA);
                if (elytraSlot != -1) {
                    InventoryUtils.pickup(elytraSlot);
                    InventoryUtils.placeItem(CHEST_SLOT_INDEX);

                    int slotIndex = hotbarSlot.get() - 1;
                    int invSlot = InventoryUtils.getHotbarOffset() + slotIndex;
                    if (MC.player.getInventory().getItem(slotIndex).isEmpty()) {
                        InventoryUtils.placeItem(invSlot);
                    } else {
                        moveToAnyEmptySlot();
                    }
                }
            }
        }
    }

    private void moveToAnyEmptySlot() {
        int emptySlot = InventoryUtils.findEmptySlotInHotbar(0);
        if (emptySlot != -1) {
            InventoryUtils.placeItem(InventoryUtils.getHotbarOffset() + emptySlot);
            return;
        }

        for (int i = InventoryUtils.getInventoryOffset(); i < MC.player.containerMenu.slots.size(); i++) {
            if (MC.player.containerMenu.getSlot(i).getItem().isEmpty()) {
                InventoryUtils.placeItem(i);
                return;
            }
        }
    }
}
