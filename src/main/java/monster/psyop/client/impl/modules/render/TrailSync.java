package monster.psyop.client.impl.modules.render;

import com.google.gson.reflect.TypeToken;
import monster.psyop.client.config.Config;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.framework.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TrailSync {

    private static final Path CONFIG_DIR = Minecraft.getInstance().gameDirectory.toPath().resolve("psyop");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("trail-sync.json");
    private static final byte[] IV = new byte[16];
    private static final Type MAP_TYPE = new TypeToken<Map<UUID, TrailData>>() {}.getType();

    private final Map<UUID, TrailData> playerTrails = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerLastSync = new ConcurrentHashMap<>();
    private final String syncPrefix;
    private final String secretKey;
    private final Module parent;

    public TrailSync(Module parent, String syncPrefix, String secretKey) {
        this.parent = parent;
        this.syncPrefix = syncPrefix;
        this.secretKey = secretKey;
        loadConfig();
    }

    public void onPacket(OnPacket.Received event) {
        if (event.packet() instanceof ClientboundSystemChatPacket packet) {
            String message = packet.content().getString();
            System.out.println("[TrailSync DEBUG] Received system chat packet: " + message);

            int prefixIndex = message.indexOf(syncPrefix);
            if (prefixIndex != -1) {
                System.out.println("[TrailSync DEBUG] Detected sync prefix!");
                String dataPart = message.substring(prefixIndex + syncPrefix.length()).trim();
                String[] parts = dataPart.split(" ");
                if (parts.length > 0) {
                    System.out.println("[TrailSync DEBUG] Data found. Attempting to deserialize...");
                    deserialize(parts[0]);
                } else {
                    System.out.println("[TrailSync DEBUG] No data found after prefix.");
                }
            }
        }
    }

    private String decrypt(byte[] encryptedData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));
            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("[TrailSync DEBUG] Decryption failed: " + e.getMessage());
            return null;
        }
    }

    private void deserialize(String data) {
        System.out.println("[TrailSync DEBUG] Raw Base64 data: " + data);
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            String decryptedData = decrypt(decodedBytes);

            if (decryptedData == null) {
                System.out.println("[TrailSync DEBUG] Deserialization failed: Decrypted data is null.");
                return;
            }
            System.out.println("[TrailSync DEBUG] Decrypted data string: " + decryptedData);

            String[] parts = decryptedData.split(";");

            if (parts.length == 8) {
                System.out.println("[TrailSync DEBUG] Data has correct number of parts (8).");
                UUID playerUUID = UUID.fromString(parts[0]);

                if (isRateLimited(playerUUID)) {
                    System.out.println("[TrailSync DEBUG] Player " + playerUUID + " is rate limited. Ignoring.");
                    return;
                }

                boolean circleMode = Boolean.parseBoolean(parts[1]);
                float circleRadius = Float.parseFloat(parts[2]);
                float circleSpeed = Float.parseFloat(parts[3]);
                boolean circleVerticalMove = Boolean.parseBoolean(parts[4]);
                float circleVerticalSpeed = Float.parseFloat(parts[5]);
                float verticalOffset = Float.parseFloat(parts[6]);
                List<Integer> enabledParticleIndices = new ArrayList<>();
                if (!parts[7].isEmpty()) {
                    enabledParticleIndices = Arrays.stream(parts[7].split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                }

                TrailData trailData = new TrailData(circleMode, circleRadius, circleSpeed, circleVerticalMove, circleVerticalSpeed, verticalOffset, enabledParticleIndices);
                playerTrails.put(playerUUID, trailData);
                playerLastSync.put(playerUUID, System.currentTimeMillis());
                System.out.println("[TrailSync DEBUG] Successfully deserialized and stored data for " + playerUUID);
                saveConfig();
            } else {
                System.out.println("[TrailSync DEBUG] Deserialization failed: Incorrect number of parts. Expected 8, got " + parts.length);
            }
        } catch (Exception e) {
            System.out.println("[TrailSync DEBUG] CRITICAL: An exception occurred during deserialization.");
            e.printStackTrace();
        }
    }

    private boolean isRateLimited(UUID playerUUID) {
        long now = System.currentTimeMillis();
        long lastSync = playerLastSync.getOrDefault(playerUUID, 0L);
        return (now - lastSync) < 1000;
    }

    private void saveConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
            String json = Config.GSON.toJson(playerTrails);
            Files.write(CONFIG_FILE, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            // Failed to save
        }
    }

    private void loadConfig() {
        if (!Files.exists(CONFIG_FILE)) return;
        try {
            String json = Files.readString(CONFIG_FILE);
            Map<UUID, TrailData> loadedMap = Config.GSON.fromJson(json, MAP_TYPE);
            if (loadedMap != null) {
                playerTrails.putAll(loadedMap);
            }
        } catch (Exception e) {
            // Failed to load
        }
    }

    public Map<UUID, TrailData> getPlayerTrails() {
        return playerTrails;
    }

    public static class TrailData {
        public final boolean circleMode;
        public final float circleRadius;
        public final float circleSpeed;
        public final boolean circleVerticalMove;
        public final float circleVerticalSpeed;
        public final float verticalOffset;
        public final List<Integer> enabledParticleIndices;

        public TrailData(boolean circleMode, float circleRadius, float circleSpeed, boolean circleVerticalMove, float circleVerticalSpeed, float verticalOffset, List<Integer> enabledParticleIndices) {
            this.circleMode = circleMode;
            this.circleRadius = circleRadius;
            this.circleSpeed = circleSpeed;
            this.circleVerticalMove = circleVerticalMove;
            this.circleVerticalSpeed = circleVerticalSpeed;
            this.verticalOffset = verticalOffset;
            this.enabledParticleIndices = enabledParticleIndices;
        }
    }
}
