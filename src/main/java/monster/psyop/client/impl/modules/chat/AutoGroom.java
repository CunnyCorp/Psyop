package monster.psyop.client.impl.modules.chat;

import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.utility.CollectionUtils;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoGroom extends Module {
    public FloatSetting delay = new FloatSetting.Builder()
            .name("delay")
            .defaultTo(45000)
            .range(1000, 120000)
            .addTo(coreGroup);
    public BoolSetting groomFriends = new BoolSetting.Builder()
            .name("groom-friends")
            .defaultTo(false)
            .addTo(coreGroup);
    public BoolSetting onlyPoor = new BoolSetting.Builder()
            .name("only-poor")
            .defaultTo(false)
            .addTo(coreGroup);
    public BoolSetting whisper = new BoolSetting.Builder()
            .name("whisper")
            .defaultTo(false)
            .addTo(coreGroup);
    public FloatSetting range = new FloatSetting.Builder()
            .name("range")
            .defaultTo(12.0f)
            .range(5.0f, 32.0f)
            .addTo(coreGroup);

    public List<String> groomingMessages = new ArrayList<>();
    public long lastTimestamp = 0L;
    public boolean broken = false;

    public AutoGroom() {
        super(Categories.CHAT, "auto-groom", "Module inspired by Future");
        URL url = getClass().getResource("/grooming.txt");
        if (url == null) {
            broken = true;
            return;
        }

        try {
            List<String> strings = Files.readAllLines(Path.of(url.toURI()));

            for (String msg : strings) {
                if (msg.startsWith("#")) continue;
                if (msg.isBlank()) continue;

                groomingMessages.add(msg);
            }
        } catch (IOException | URISyntaxException e) {
            broken = true;
        }

    }

    @Override
    public void update() {
        if (broken) return;

        if (System.currentTimeMillis() - lastTimestamp < delay.get()) {
            return;
        }

        if (getVictim() != null) {
            lastTimestamp = System.currentTimeMillis();
            String victim = getVictim();

            String message = CollectionUtils.random(groomingMessages).replaceAll("<player>", victim);

            if (whisper.get()) {
                PacketUtils.command("w " + victim + " " + message);
            } else {
                PacketUtils.chat(message, false);
            }
        }
    }

    public String getVictim() {
        List<Entity> entities = MC.level.getEntities(MC.player,
                new AABB(MC.player.getX() - range.get(), MC.player.getY() - 128, MC.player.getZ() - range.get(), MC.player.getX() + range.get(), MC.player.getY() + 128, MC.player.getZ() + range.get()),
                entity -> {
                    if (entity instanceof Player player) {
                        if (!groomFriends.get() && !FriendManager.canAttack(player)) return false;
                        if (onlyPoor.get() && player.getArmorValue() > 5 && MC.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
                            return false;
                        }

                        return true;
                    }

                    return false;
                });

        entities.sort(Comparator.comparingDouble(e -> e.distanceTo(MC.player)));

        return entities.isEmpty() ? null : entities.get(0).getDisplayName().getString();
    }
}
