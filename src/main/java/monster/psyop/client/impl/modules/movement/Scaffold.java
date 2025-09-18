package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.events.game.OnTick;

public class Scaffold extends Module {

    public Scaffold() {
        super(Categories.MOVEMENT, "sneak", "Automatically sneaks.");
    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        if (MC.player == null) {
        }
    }
}
