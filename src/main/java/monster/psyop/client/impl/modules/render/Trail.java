package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.particles.SculkChargeParticleOptions;

import java.util.LinkedHashMap;
import java.util.Map;

public class Trail extends Module {

    private final GroupedSettings generalGroup = addGroup(new GroupedSettings("general", "General trail settings"));
    private final BoolSetting pauseWhenStationary = new BoolSetting.Builder().name("pause-when-stationary").description("Don't spawn particles while standing still for the linear trail.").defaultTo(true).addTo(generalGroup);
    private final FloatSetting verticalOffset = new FloatSetting.Builder().name("vertical-offset").description("Height offset for the particles relative to the player.").range(-2.0f, 2.0f).defaultTo(0.1f).addTo(generalGroup);

    private final GroupedSettings circleGroup = addGroup(new GroupedSettings("circle", "Settings for the circle mode"));
    public final BoolSetting circleMode = new BoolSetting.Builder().name("circle-mode").description("Spawns particles in a circle around the player.").defaultTo(false).addTo(circleGroup);
    public final FloatSetting circleRadius = new FloatSetting.Builder().name("circle-radius").description("The radius of the particle circle.").defaultTo(1.5f).range(0.5f, 5.0f).addTo(circleGroup);
    public final FloatSetting circleSpeed = new FloatSetting.Builder().name("circle-speed").description("How fast the particles orbit the player.").defaultTo(0.2f).range(0.05f, 2.0f).addTo(circleGroup);
    public final BoolSetting circleVerticalMove = new BoolSetting.Builder().name("circle-vertical-move").description("Makes the circle move up and down.").defaultTo(false).addTo(circleGroup);
    public final FloatSetting circleVerticalSpeed = new FloatSetting.Builder().name("circle-vertical-speed").description("How fast the circle moves up and down.").defaultTo(0.1f).range(0.01f, 1.0f).addTo(circleGroup);

    private final GroupedSettings particlesGroup = addGroup(new GroupedSettings("particles", "Which particles to show"));
    private final Map<BoolSetting, ParticleOptions> particleMap = new LinkedHashMap<>();

    private double prevX = Double.NaN;
    private double prevY = Double.NaN;
    private double prevZ = Double.NaN;
    private double circleAngle = 0;
    private double verticalAngle = 0;

    public Trail() {
        super(Categories.RENDER, "trail", "Renders a customizable particle trail behind your player.");

        addParticle("angry-villager", ParticleTypes.ANGRY_VILLAGER);
        addParticle("ash", ParticleTypes.ASH);
        addParticle("bubble", ParticleTypes.BUBBLE);
        addParticle("bubble-column-up", ParticleTypes.BUBBLE_COLUMN_UP);
        addParticle("bubble-pop", ParticleTypes.BUBBLE_POP);
        addParticle("campfire-cosy-smoke", ParticleTypes.CAMPFIRE_COSY_SMOKE);
        addParticle("campfire-signal-smoke", ParticleTypes.CAMPFIRE_SIGNAL_SMOKE);
        addParticle("cherry-leaves", ParticleTypes.CHERRY_LEAVES);
        addParticle("cloud", ParticleTypes.CLOUD);
        addParticle("composter", ParticleTypes.COMPOSTER);
        addParticle("crimson-spore", ParticleTypes.CRIMSON_SPORE);
        addParticle("crit", ParticleTypes.CRIT);
        addParticle("current-down", ParticleTypes.CURRENT_DOWN);
        addParticle("damage-indicator", ParticleTypes.DAMAGE_INDICATOR);
        addParticle("dolphin", ParticleTypes.DOLPHIN);
        addParticle("dragon-breath", ParticleTypes.DRAGON_BREATH);
        addParticle("dripping-dripstone-lava", ParticleTypes.DRIPPING_DRIPSTONE_LAVA);
        addParticle("dripping-dripstone-water", ParticleTypes.DRIPPING_DRIPSTONE_WATER);
        addParticle("dripping-lava", ParticleTypes.DRIPPING_LAVA);
        addParticle("dripping-obsidian-tear", ParticleTypes.DRIPPING_OBSIDIAN_TEAR);
        addParticle("dripping-water", ParticleTypes.DRIPPING_WATER);
        addParticle("effect", ParticleTypes.EFFECT);
        addParticle("elder-guardian", ParticleTypes.ELDER_GUARDIAN);
        addParticle("electric-spark", ParticleTypes.ELECTRIC_SPARK);
        addParticle("enchant", ParticleTypes.ENCHANT);
        addParticle("enchanted-hit", ParticleTypes.ENCHANTED_HIT);
        addParticle("end-rod", ParticleTypes.END_ROD);
        addParticle("explosion", ParticleTypes.EXPLOSION);
        addParticle("explosion-emitter", ParticleTypes.EXPLOSION_EMITTER);
        addParticle("falling-dripstone-lava", ParticleTypes.FALLING_DRIPSTONE_LAVA);
        addParticle("falling-dripstone-water", ParticleTypes.FALLING_DRIPSTONE_WATER);
        addParticle("falling-lava", ParticleTypes.FALLING_LAVA);
        addParticle("falling-obsidian-tear", ParticleTypes.FALLING_OBSIDIAN_TEAR);
        addParticle("falling-spore-blossom", ParticleTypes.FALLING_SPORE_BLOSSOM);
        addParticle("falling-water", ParticleTypes.FALLING_WATER);
        addParticle("firework", ParticleTypes.FIREWORK);
        addParticle("fishing", ParticleTypes.FISHING);
        addParticle("flame", ParticleTypes.FLAME);
        addParticle("flash", ParticleTypes.FLASH);
        addParticle("glow", ParticleTypes.GLOW);
        addParticle("glow-squid-ink", ParticleTypes.GLOW_SQUID_INK);
        addParticle("happy-villager", ParticleTypes.HAPPY_VILLAGER);
        addParticle("heart", ParticleTypes.HEART);
        addParticle("instant-effect", ParticleTypes.INSTANT_EFFECT);
        addParticle("item-slime", ParticleTypes.ITEM_SLIME);
        addParticle("item-snowball", ParticleTypes.ITEM_SNOWBALL);
        addParticle("landing-lava", ParticleTypes.LANDING_LAVA);
        addParticle("landing-obsidian-tear", ParticleTypes.LANDING_OBSIDIAN_TEAR);
        addParticle("large-smoke", ParticleTypes.LARGE_SMOKE);
        addParticle("lava", ParticleTypes.LAVA);
        addParticle("mycelium", ParticleTypes.MYCELIUM);
        addParticle("nautilus", ParticleTypes.NAUTILUS);
        addParticle("note", ParticleTypes.NOTE);
        addParticle("poof", ParticleTypes.POOF);
        addParticle("portal", ParticleTypes.PORTAL);
        addParticle("rain", ParticleTypes.RAIN);
        addParticle("redstone", DustParticleOptions.REDSTONE);
        addParticle("scrape", ParticleTypes.SCRAPE);
        addParticle("sculk-charge", new SculkChargeParticleOptions(1.0f));
        addParticle("sculk-charge-pop", ParticleTypes.SCULK_CHARGE_POP);
        addParticle("sculk-soul", ParticleTypes.SCULK_SOUL);
        addParticle("shriek", new ShriekParticleOption(0));
        addParticle("smoke", ParticleTypes.SMOKE);
        addParticle("sneeze", ParticleTypes.SNEEZE);
        addParticle("snowflake", ParticleTypes.SNOWFLAKE);
        addParticle("sonic-boom", ParticleTypes.SONIC_BOOM);
        addParticle("soul", ParticleTypes.SOUL);
        addParticle("soul-fire-flame", ParticleTypes.SOUL_FIRE_FLAME);
        addParticle("spit", ParticleTypes.SPIT);
        addParticle("splash", ParticleTypes.SPLASH);
        addParticle("spore-blossom-air", ParticleTypes.SPORE_BLOSSOM_AIR);
        addParticle("squid-ink", ParticleTypes.SQUID_INK);
        addParticle("sweep-attack", ParticleTypes.SWEEP_ATTACK);
        addParticle("totem-of-undying", ParticleTypes.TOTEM_OF_UNDYING);
        addParticle("underwater", ParticleTypes.UNDERWATER);
        addParticle("warped-spore", ParticleTypes.WARPED_SPORE);
        addParticle("wax-off", ParticleTypes.WAX_OFF);
        addParticle("wax-on", ParticleTypes.WAX_ON);
        addParticle("white-ash", ParticleTypes.WHITE_ASH);
        addParticle("witch", ParticleTypes.WITCH);
    }

    private void addParticle(String name, ParticleOptions options) {
        BoolSetting setting = new BoolSetting.Builder().name(name).defaultTo(false).addTo(particlesGroup);
        particleMap.put(setting, options);
    }

    @Override
    public void update() {
        if (MC.player == null || MC.level == null) return;

        if (circleMode.get()) {
            // Horizontal circle movement
            circleAngle += circleSpeed.get();
            if (circleAngle > Math.PI * 2) circleAngle -= Math.PI * 2;

            double offsetX = Math.cos(circleAngle) * circleRadius.get();
            double offsetZ = Math.sin(circleAngle) * circleRadius.get();

            double px = MC.player.getX() + offsetX;
            double pz = MC.player.getZ() + offsetZ;

            // Vertical movement
            double py;
            if (circleVerticalMove.get()) {
                verticalAngle += circleVerticalSpeed.get();
                if (verticalAngle > Math.PI * 2) verticalAngle -= Math.PI * 2;
                // Use a sine wave to create a smooth up-and-down motion from 0 to 1
                double verticalFactor = (Math.sin(verticalAngle) + 1) / 2.0;
                py = MC.player.getY() + (verticalFactor * verticalOffset.get());
            } else {
                py = MC.player.getY() + verticalOffset.get();
            }

            spawnParticles(px, py, pz);
        } else {
            // Linear Trail Logic
            double x = MC.player.getX();
            double y = MC.player.getY();
            double z = MC.player.getZ();

            boolean stationary = !Double.isNaN(prevX) && (x == prevX && y == prevY && z == prevZ);

            prevX = x;
            prevY = y;
            prevZ = z;

            if (pauseWhenStationary.get() && stationary) return;

            double py = y + verticalOffset.get();
            spawnParticles(x, py, z);
        }
    }

    private void spawnParticles(double x, double y, double z) {
        for (Map.Entry<BoolSetting, ParticleOptions> entry : particleMap.entrySet()) {
            if (entry.getKey().get()) {
                MC.level.addParticle(entry.getValue(), x, y, z, 0, 0, 0);
            }
        }
    }
}
