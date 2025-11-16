package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.impl.events.game.OnMove;

public class Glide extends Module {
    public FloatSetting maxFallSpeed = new FloatSetting.Builder()
            .name("max-fall-speed")
            .defaultTo(0.082f)
            .range(0.0f, 0.2f)
            .addTo(coreGroup);

    public Glide() {
        super(Categories.MOVEMENT, "glide", "Fall slowly!");
    }

    @EventListener(priority = 1000)
    public void onPlayerMove(OnMove.Player event) {
        event.vec3.y = Math.max(-maxFallSpeed.get(), event.vec3.y);
    }

    @Override
    public AntiCheat getAntiCheat() {
        return AntiCheat.Veck_Grim;
    }
}
