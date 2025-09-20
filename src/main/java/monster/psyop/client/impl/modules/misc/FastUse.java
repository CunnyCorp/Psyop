package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.events.game.OnTick;

public class FastUse extends Module {
    public FastUse() {
        super(Categories.MISC, "fast-use", "Removes the delay between using items.");
    }

    @EventListener
    public void onTickPre(OnTick.Pre event) {
        if (MC.options.keyUse.isDown()) {
            MC.rightClickDelay = 0;
        }
    }
}
