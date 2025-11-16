package monster.psyop.client.impl.modules.silly;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import net.minecraft.world.InteractionHand;

public class HappyHands extends Module {
    public HappyHands() {
        super(Categories.SILLY, "happy-hands", "Happy ! Happy ! Happy !");
    }

    @Override
    public void update() {
        if (Psyop.RANDOM.nextInt(0, 12) >= 10) {
            MC.player.swing(InteractionHand.OFF_HAND, true);
        }
    }
}
