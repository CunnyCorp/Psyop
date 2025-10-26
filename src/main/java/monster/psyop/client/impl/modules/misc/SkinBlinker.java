package monster.psyop.client.impl.modules.misc;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.mixin.OptionsAccessor;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.HashSet;
import java.util.Set;

public class SkinBlinker extends Module {

    private final IntSetting delay = new IntSetting.Builder()
        .name("delay-ticks")
        .description("The delay in ticks between blinks.")
        .defaultTo(40)
        .range(0, 200)
        .addTo(coreGroup);

    private final BoolSetting excludeCapes = new BoolSetting.Builder()
        .name("exclude-capes")
        .description("Don't blink capes.")
        .defaultTo(true)
        .addTo(coreGroup);

    private int timer = 0;
    private final Set<PlayerModelPart> enabledParts = new HashSet<>();

    public SkinBlinker() {
        super(Categories.MISC, "skin-blinker", "Toggles skin layers to make your character blink.");
    }

    @Override
    public void enabled() {
        super.enabled();
        if (MC.options == null) return;

        timer = 0;
        enabledParts.clear();
        enabledParts.addAll(((OptionsAccessor) MC.options).getModelParts());
    }

    @Override
    public void disabled() {
        super.disabled();
        if (MC.options == null) return;

        timer = 0;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (excludeCapes.get() && part == PlayerModelPart.CAPE) continue;

            MC.options.setModelPart(part, enabledParts.contains(part));
        }
        MC.options.broadcastOptions();
    }

    @Override
    public void update() {
        if (MC.player == null || MC.options == null) return;

        timer++;
        if (timer < delay.get()) return;

        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (excludeCapes.get() && part == PlayerModelPart.CAPE) continue;

            boolean currentState = MC.options.isModelPartEnabled(part);
            MC.options.setModelPart(part, !currentState);
        }

        MC.options.broadcastOptions();
        timer = 0;
    }
}
