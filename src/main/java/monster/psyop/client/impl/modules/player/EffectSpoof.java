package monster.psyop.client.impl.modules.player;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class EffectSpoof extends Module {

    // Haste
    public final GroupedSettings hasteGroup = addGroup(new GroupedSettings("haste", "Spoof Haste (Dig Speed) effect"));
    public final BoolSetting spoofHaste = new BoolSetting.Builder()
            .name("spoof-haste")
            .description("Enable Haste effect spoofing.")
            .defaultTo(false)
            .addTo(hasteGroup);
    public final IntSetting hasteLevel = new IntSetting.Builder()
            .name("haste-level")
            .description("The level of Haste to spoof.")
            .defaultTo(1)
            .range(1, 255)
            .addTo(hasteGroup);

    // Night Vision
    public final GroupedSettings nightVisionGroup = addGroup(new GroupedSettings("night-vision", "Spoof Night Vision effect"));
    public final BoolSetting spoofNightVision = new BoolSetting.Builder()
            .name("spoof-night-vision")
            .description("Enable Night Vision effect spoofing.")
            .defaultTo(false)
            .addTo(nightVisionGroup);

    // Luck
    public final GroupedSettings luckGroup = addGroup(new GroupedSettings("luck", "Spoof Luck effect"));
    public final BoolSetting spoofLuck = new BoolSetting.Builder()
            .name("spoof-luck")
            .description("Enable Luck effect spoofing.")
            .defaultTo(false)
            .addTo(luckGroup);

    // Unluck
    public final GroupedSettings unluckGroup = addGroup(new GroupedSettings("unluck", "Spoof Unluck effect"));
    public final BoolSetting spoofUnluck = new BoolSetting.Builder()
            .name("spoof-unluck")
            .description("Enable Unluck effect spoofing.")
            .defaultTo(false)
            .addTo(unluckGroup);


    public EffectSpoof() {
        super(Categories.PLAYER, "effect-spoof", "Spoofs client-side potion effects.");
    }

    @Override
    public void update() {
        if (MC.player == null) return;

        handleEffect(spoofHaste.get(), MobEffects.HASTE, hasteLevel.get());
        handleEffect(spoofNightVision.get(), MobEffects.NIGHT_VISION, 1); // Night Vision has no levels
        handleEffect(spoofLuck.get(), MobEffects.LUCK, 1); // Luck has no levels
        handleEffect(spoofUnluck.get(), MobEffects.UNLUCK, 1); // Unluck has no levels
    }

    private void handleEffect(boolean shouldSpoof, Holder<MobEffect> effect, int level) {
        if (shouldSpoof) {
            MobEffectInstance currentEffect = MC.player.getEffect(effect);
            int desiredAmplifier = level - 1;

            // Re-apply if effect is missing or has wrong amplifier.
            if (currentEffect == null || currentEffect.getAmplifier() != desiredAmplifier) {
                // Duration -1 is often used for infinite effects. showIcon is true to see it.
                MC.player.addEffect(new MobEffectInstance(effect, -1, desiredAmplifier, false, true, true));
            }
        } else {
            if (MC.player.hasEffect(effect)) {
                MC.player.removeEffect(effect);
            }
        }
    }

    @Override
    protected void disabled() {
        super.disabled();
        if (MC.player != null) {
            if (MC.player.hasEffect(MobEffects.HASTE)) MC.player.removeEffect(MobEffects.HASTE);
            if (MC.player.hasEffect(MobEffects.NIGHT_VISION)) MC.player.removeEffect(MobEffects.NIGHT_VISION);
            if (MC.player.hasEffect(MobEffects.LUCK)) MC.player.removeEffect(MobEffects.LUCK);
            if (MC.player.hasEffect(MobEffects.UNLUCK)) MC.player.removeEffect(MobEffects.UNLUCK);
        }
    }
}
