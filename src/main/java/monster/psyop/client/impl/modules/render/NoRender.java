package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;

public class NoRender extends Module {
    public final BoolSetting beams = new BoolSetting.Builder()
            .name("beams")
            .description("Prevents beam rendering.")
            .defaultTo(false)
            .addTo(coreGroup);
    public final BoolSetting explosions = new BoolSetting.Builder()
            .name("explosions")
            .description("Prevents explosion particles from rendering")
            .defaultTo(false)
            .addTo(coreGroup);
    public final BoolSetting playerStatus = new BoolSetting.Builder()
            .name("player-status")
            .description("Hides all player status info.")
            .defaultTo(false)
            .addTo(coreGroup);
    public final BoolSetting healthBar = new BoolSetting.Builder()
            .name("health-bar")
            .description("Hides the health bar.")
            .defaultTo(false)
            .addTo(coreGroup);
    public final BoolSetting hungerBar = new BoolSetting.Builder()
            .name("hunger-bar")
            .description("Hides the hunger bar.")
            .defaultTo(false)
            .addTo(coreGroup);



    public NoRender() {
        super(Categories.RENDER, "no-render", "Disables rendering of certain things.");
    }
}
