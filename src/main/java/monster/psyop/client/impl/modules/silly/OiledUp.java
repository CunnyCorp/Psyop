package monster.psyop.client.impl.modules.silly;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.events.game.OnMove;
import monster.psyop.client.utility.CollectionUtils;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OiledUp extends Module {
    public OiledUp() {
        super(Categories.SILLY, "oiled-up", "You oiled up so make it jiggle!");
    }

    @EventListener
    public void onMove(OnMove.Player event) {
        if (Psyop.RANDOM.nextInt(0, 100) == 69) {
            if (!MC.player.getMainHandItem().isEmpty()) {
                MC.player.drop(true);
            } else {
                List<Item> itemList = new ArrayList<>();

                for (ItemStack stack : MC.player.containerMenu.getItems()) {
                    if (!itemList.contains(stack.getItem())) {
                        itemList.add(stack.getItem());
                    }
                }

                int slot = InventoryUtils.findAnySlot(CollectionUtils.random(itemList));

                if (slot == -1) {
                    return;
                }

                InventoryUtils.dropSlot(slot, true);
            }

            NotificationManager.get().addNotification(getLabel(), "You were too oily", NotificationEvent.Type.SUCCESS, 3000L);
        }
    }
}
