package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.events.game.OnTick;

public class DetachMouse extends Module {
    public DetachMouse() {
        super(Categories.MISC, "detach-mouse", "Detaches the mouse from the game.");
    }

    @EventListener
    public void onTick(OnTick.Pre event) {
        if (MC.mouseHandler.isMouseGrabbed()) {
            MC.mouseHandler.releaseMouse();
        }
    }
}
