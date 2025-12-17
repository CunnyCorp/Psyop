package monster.psyop.client.impl.modules.movement;

import imgui.type.ImString;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.ProvidedStringSetting;

import java.util.List;

public class PlayerTimer extends Module {
    public IntSetting multiplier = new IntSetting.Builder()
            .name("multiplier")
            .description("How many extra ticks to run.")
            .defaultTo(3)
            .addTo(coreGroup);
    public IntSetting burstMultiplier = new IntSetting.Builder()
            .name("burst-multiplier")
            .description("How many times to burst.")
            .defaultTo(5)
            .addTo(coreGroup);
    public IntSetting burstDelay = new IntSetting.Builder()
            .name("burst-delay")
            .description("How long between bursts.")
            .defaultTo(10)
            .addTo(coreGroup);
    public ProvidedStringSetting elytraMode = new ProvidedStringSetting.Builder()
            .suggestions(List.of(new ImString("None"), new ImString("Lock"), new ImString("Disabler")))
            .name("elytra-mode")
            .defaultTo(new ImString("None"))
            .addTo(coreGroup);

    public int lastBurst = 0;

    public PlayerTimer() {
        super(Categories.MOVEMENT, "player-timer", "Sends multiple player ticks.");
    }

    @Override
    public void update() {
        if (lastBurst > 1) {
            lastBurst--;
        }
    }

    @Override
    public AntiCheat getAntiCheat() {
        return AntiCheat.Fraze_Grim;
    }
}
