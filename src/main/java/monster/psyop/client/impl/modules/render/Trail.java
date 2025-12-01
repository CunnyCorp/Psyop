package monster.psyop.client.impl.modules.render;

import imgui.type.ImBoolean;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import net.minecraft.core.particles.*;
import net.minecraft.world.entity.player.Player;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Trail extends Module {

    private static final String SECRET_KEY = "a_very_secret_and_janky_key";
    private static final byte[] IV = new byte[16];

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
    private final List<ParticleOptions> particleOptionsByIndex;

    private final GroupedSettings syncGroup = addGroup(new GroupedSettings("sync", "Synchronization settings"));
    private final BoolSetting syncEnabled = new BoolSetting.Builder().name("sync-enabled").description("Sync trail with other users.").defaultTo(false).addTo(syncGroup);
    private final BoolSetting syncNow = new BoolSetting.Builder().name("sync-now").description("Send your trail data to other players.").defaultTo(false).addTo(syncGroup);
    private final String syncPrefix = "$sync";

    private final TrailSync trailSync;
    private final Map<UUID, PlayerTrailState> playerStates = new ConcurrentHashMap<>();

    private double localCircleAngle = 0;
    private double localVerticalAngle = 0;
    private double prevX = Double.NaN;
    private double prevY = Double.NaN;
    private double prevZ = Double.NaN;

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

        this.particleOptionsByIndex = new ArrayList<>(particleMap.values());
        this.trailSync = new TrailSync(this, syncPrefix, SECRET_KEY);
    }

    private void addParticle(String name, ParticleOptions options) {
        BoolSetting setting = new BoolSetting.Builder().name(name).defaultTo(false).addTo(particlesGroup);
        particleMap.put(setting, options);
    }

    @EventListener
    public void onPacket(OnPacket.Received event) {
        if (syncEnabled.get()) {
            trailSync.onPacket(event);
        }
    }

    @Override
    public void update() {
        if (MC.player == null || MC.level == null) return;

        updateLocalTrail();

        if (syncEnabled.get()) {
            if (syncNow.get()) {
                sendTrailData();
                syncNow.value(new ImBoolean(false));
            }
            renderSyncedTrails();
        }
    }

    private void updateLocalTrail() {
        List<Integer> enabledParticleIndices = getEnabledParticleIndices();

        if (circleMode.get()) {
            localCircleAngle += circleSpeed.get();
            if (localCircleAngle > Math.PI * 2) localCircleAngle -= Math.PI * 2;

            double offsetX = Math.cos(localCircleAngle) * circleRadius.get();
            double offsetZ = Math.sin(localCircleAngle) * circleRadius.get();

            double px = MC.player.getX() + offsetX;
            double pz = MC.player.getZ() + offsetZ;

            double py;
            if (circleVerticalMove.get()) {
                localVerticalAngle += circleVerticalSpeed.get();
                if (localVerticalAngle > Math.PI * 2) localVerticalAngle -= Math.PI * 2;
                double verticalFactor = (Math.sin(localVerticalAngle) + 1) / 2.0;
                py = MC.player.getY() + (verticalFactor * verticalOffset.get());
            } else {
                py = MC.player.getY() + verticalOffset.get();
            }

            spawnParticles(px, py, pz, enabledParticleIndices);
        } else {
            double x = MC.player.getX();
            double y = MC.player.getY();
            double z = MC.player.getZ();

            boolean stationary = !Double.isNaN(prevX) && (x == prevX && y == prevY && z == prevZ);

            prevX = x;
            prevY = y;
            prevZ = z;

            if (pauseWhenStationary.get() && stationary) return;

            double py = y + verticalOffset.get();
            spawnParticles(x, py, z, enabledParticleIndices);
        }
    }

    private void renderSyncedTrails() {
        playerStates.keySet().removeIf(uuid -> MC.level.getPlayerByUUID(uuid) == null);

        for (Map.Entry<UUID, TrailSync.TrailData> entry : trailSync.getPlayerTrails().entrySet()) {
            UUID playerUUID = entry.getKey();
            if (playerUUID.equals(MC.player.getUUID())) continue;

            Player player = MC.level.getPlayerByUUID(playerUUID);
            if (player == null) continue;

            TrailSync.TrailData trailData = entry.getValue();
            PlayerTrailState state = playerStates.computeIfAbsent(playerUUID, k -> new PlayerTrailState());

            if (trailData.circleMode) {
                state.circleAngle += trailData.circleSpeed;
                if (state.circleAngle > Math.PI * 2) state.circleAngle -= Math.PI * 2;

                double offsetX = Math.cos(state.circleAngle) * trailData.circleRadius;
                double offsetZ = Math.sin(state.circleAngle) * trailData.circleRadius;

                double px = player.getX() + offsetX;
                double pz = player.getZ() + offsetZ;

                double py;
                if (trailData.circleVerticalMove) {
                    state.verticalAngle += trailData.circleVerticalSpeed;
                    if (state.verticalAngle > Math.PI * 2) state.verticalAngle -= Math.PI * 2;
                    double verticalFactor = (Math.sin(state.verticalAngle) + 1) / 2.0;
                    py = player.getY() + (verticalFactor * trailData.verticalOffset);
                } else {
                    py = player.getY() + trailData.verticalOffset;
                }
                spawnParticles(px, py, pz, trailData.enabledParticleIndices);
            } else {
                spawnParticles(player.getX(), player.getY() + trailData.verticalOffset, player.getZ(), trailData.enabledParticleIndices);
            }
        }
    }

    private void spawnParticles(double x, double y, double z, List<Integer> enabledParticleIndices) {
        for (int index : enabledParticleIndices) {
            if (index >= 0 && index < particleOptionsByIndex.size()) {
                MC.level.addParticle(particleOptionsByIndex.get(index), x, y, z, 0, 0, 0);
            }
        }
    }

    private List<Integer> getEnabledParticleIndices() {
        List<Integer> indices = new ArrayList<>();
        int i = 0;
        for (Map.Entry<BoolSetting, ParticleOptions> entry : particleMap.entrySet()) {
            if (entry.getKey().get()) {
                indices.add(i);
            }
            i++;
        }
        return indices;
    }

    private byte[] encrypt(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));
            return cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    private String serialize(UUID playerUUID) {
        String enabledParticleIndices = getEnabledParticleIndices().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String rawData = String.format("%s;%s;%s;%s;%s;%s;%s;%s",
                playerUUID.toString(),
                circleMode.get(),
                circleRadius.get(),
                circleSpeed.get(),
                circleVerticalMove.get(),
                circleVerticalSpeed.get(),
                verticalOffset.get(),
                enabledParticleIndices);

        byte[] encryptedData = encrypt(rawData);
        if (encryptedData == null) return null;

        String base64Data = Base64.getEncoder().encodeToString(encryptedData);
        System.out.println("[TrailSync DEBUG] Sending data: " + base64Data);
        return base64Data;
    }

    private void sendTrailData() {
        if (MC.player != null) {
            String data = serialize(MC.player.getUUID());
            if (data != null) {
                MC.player.connection.sendChat(syncPrefix + " " + data);
            }
        }
    }

    private static class PlayerTrailState {
        double circleAngle = 0;
        double verticalAngle = 0;
    }
}
