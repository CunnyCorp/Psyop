package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.ItemListSetting;
import monster.psyop.client.utility.InventoryUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;

import java.util.List;

public class AutoEat extends Module {
    private final IntSetting dedicatedSlot =
            new IntSetting.Builder()
                    .name("dedicated-slot")
                    .description("The slot to keep food.")
                    .defaultTo(7)
                    .range(0, 8)
                    .addTo(coreGroup);
    private final IntSetting minHunger =
            new IntSetting.Builder()
                    .name("min-hunger")
                    .description("When to start eating.")
                    .defaultTo(17)
                    .range(9, 19)
                    .addTo(coreGroup);
    private final IntSetting minHealth =
            new IntSetting.Builder()
                    .name("min-health")
                    .description("When to start e-gap chugging.")
                    .defaultTo(14)
                    .range(9, 19)
                    .addTo(coreGroup);
    private final ItemListSetting food =
            new ItemListSetting.Builder()
                    .name("food")
                    .description("What you can eat.")
                    .defaultTo(List.of(Items.ENCHANTED_GOLDEN_APPLE))
                    .filter((item) -> item.getDefaultInstance().has(DataComponents.CONSUMABLE))
                    .addTo(coreGroup);

    private boolean useKeyDown = false;
    private int hasHadFor = 0;

    public AutoEat() {
        super(Categories.MISC, "auto-eat", "Automatically eats food when you are hungry.");
    }

    @Override
    public void update() {
        boolean shouldGap = MC.player.getHealth() <= minHealth.get();

        if (shouldEat()) {
            if (MC.player.getMainHandItem().has(DataComponents.CONSUMABLE) && (shouldGap || food.value().contains(MC.player.getMainHandItem().getItem()))) {
                hasHadFor++;
            } else {
                hasHadFor = 0;

                int slot = InventoryUtils.findMatchingSlot((item, s) -> shouldGap ? item.getItem() == Items.ENCHANTED_GOLDEN_APPLE : item.has(DataComponents.CONSUMABLE) && food.value().contains(item.getItem()));

                if (slot == -1) {
                    return;
                }

                InventoryUtils.swapSlot(dedicatedSlot.get());
                InventoryUtils.swapToHotbar(slot, dedicatedSlot.get());
            }


            if (hasHadFor >= 3 && MC.player.getMainHandItem().has(DataComponents.CONSUMABLE) && (shouldGap || food.value().contains(MC.player.getMainHandItem().getItem()))) {
                useKeyDown = true;
                MC.options.keyUse.setDown(true);
            }
        } else if (useKeyDown) {
            useKeyDown = false;
            MC.options.keyUse.setDown(false);
        }
    }

    public boolean shouldEat() {
        return MC.player.getFoodData().getFoodLevel() < minHunger.get() || MC.player.getHealth() <= minHealth.get();
    }

    @Override
    public boolean controlsHotbar() {
        return true;
    }

    @Override
    public boolean inUse() {
        return useKeyDown || shouldEat();
    }
}
