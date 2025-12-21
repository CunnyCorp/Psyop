package monster.psyop.client.impl.modules.player;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.ItemListSetting;
import monster.psyop.client.utility.InventoryUtils;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class Offhand extends Module {
    public ItemListSetting items = new ItemListSetting.Builder()
            .name("items")
            .defaultTo(new ArrayList<>(List.of(Items.TOTEM_OF_UNDYING)))
            .addTo(coreGroup);
    public BoolSetting forceTotem = new BoolSetting.Builder()
            .name("force-totem")
            .defaultTo(false)
            .addTo(coreGroup);
    public BoolSetting nearPlayer = new BoolSetting.Builder()
            .name("near-player")
            .defaultTo(false)
            .addTo(coreGroup);
    public BoolSetting belowHealth = new BoolSetting.Builder()
            .name("below-health")
            .defaultTo(false)
            .addTo(coreGroup);
    public IntSetting health = new IntSetting.Builder()
            .name("health")
            .defaultTo(10)
            .range(0, 20)
            .addTo(coreGroup);

    public Offhand() {
        super(Categories.PLAYER, "offhand", "Automatically move items to the offhand.");
    }

    @Override
    public void update() {
        boolean shouldForceTotem = false;

        if (!items.value().contains(MC.player.getOffhandItem().getItem())) {
            if (forceTotem.get()) {
                if (belowHealth.get() && MC.player.getHealth() <= health.get()) {
                    shouldForceTotem = true;
                }

                if (nearPlayer.get()) {
                    for (AbstractClientPlayer player : MC.level.players()) {
                        if (MC.player.equals(player)) {
                            continue;
                        }

                        if (MC.player.distanceToSqr(player) <= 16) {
                            shouldForceTotem = true;
                            break;
                        }
                    }
                }

                if (shouldForceTotem) {
                    int slot = InventoryUtils.findAnySlot(Items.TOTEM_OF_UNDYING);

                    if (slot == -1) {
                        return;
                    }

                    InventoryUtils.swapToHotbar(slot, 40);
                    return;
                }
            }

            int slot = InventoryUtils.findAnySlot((stack) -> items.value().contains(stack.getItem()) && stack.getItem() != Items.TOTEM_OF_UNDYING);

            if (slot == -1) {
                slot = InventoryUtils.findAnySlot(Items.TOTEM_OF_UNDYING);
            }

            if (slot == -1) {
                return;
            }

            InventoryUtils.swapToHotbar(slot, 40);
        }
    }
}
