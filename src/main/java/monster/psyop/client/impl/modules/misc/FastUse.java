package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BlockListSetting;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ItemListSetting;
import monster.psyop.client.impl.events.game.OnTick;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.ArrayList;

public class FastUse extends Module {
    public BoolSetting blocks = new BoolSetting.Builder()
            .name("blocks")
            .description("Fast placing.")
            .defaultTo(true)
            .addTo(coreGroup);
    public BoolSetting useWhitelist = new BoolSetting.Builder()
            .name("use-whitelist")
            .description("Only use items in the whitelist.")
            .defaultTo(false)
            .addTo(coreGroup);
    public ItemListSetting useItems = new ItemListSetting.Builder()
            .name("use-items")
            .description("The items to use.")
            .defaultTo(new ArrayList<>())
            .visible((v) -> useWhitelist.get())
            .addTo(coreGroup);

    public FastUse() {
        super(Categories.MISC, "fast-use", "Removes the delay between using items.");
    }

    @Override
    public void update() {
        if (MC.options.keyUse.isDown()) {
            ItemStack stack = MC.player.getUseItem();

            if (useWhitelist.get() && !useItems.value().contains(stack.getItem())) {
                return;
            }



            if (!blocks.get() && stack.getUseAnimation() == ItemUseAnimation.BLOCK) {
                return;
            }

            MC.rightClickDelay = 0;
        }
    }
}
