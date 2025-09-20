package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.events.game.OnTick;

public class NoMiss extends Module {
    public NoMiss() {
        super(Categories.MISC, "no-miss", "Removes miss times for blocks.");
    }

    @EventListener
    public void onTickPre(OnTick.Pre event) {
        MC.missTime = 0;
    }
}
