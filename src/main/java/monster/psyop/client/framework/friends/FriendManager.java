package monster.psyop.client.framework.friends;

import monster.psyop.client.config.sub.PlayerRole;
import monster.psyop.client.framework.friends.defaults.Enemy;
import monster.psyop.client.framework.friends.defaults.Friend;
import monster.psyop.client.framework.friends.defaults.Neutral;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendManager {
    public static Map<String, RoleType> rolesByName = new HashMap<>();
    public static List<RoleType> roleTypes = new ArrayList<>();
    public static Map<String, PlayerRole> roles = new HashMap<>();

    public static void init() {
        loadRole(new Enemy());
        loadRole(new Neutral());
        loadRole(new Friend());
    }

    public static void addRole(String uuid, String role) {
        roles.put(uuid, new PlayerRole(uuid, role));
    }

    public static void loadRole(RoleType roleType) {
        if (rolesByName.containsKey(roleType.name)) {
            return;
        }
        roleTypes.add(roleType);
        rolesByName.put(roleType.name, roleType);
    }

    public static RoleType makeNewRole(String name) {
        return new RoleType(name, "Set a description!", new ImColorW(new Color(255, 255, 255, 255)));
    }

    public static void removeFriend(String friend) {
        roles.remove(friend);
    }

    public static boolean canAttack(Player player) {
        if (roles.containsKey(player.getUUID().toString())) {
            return roles.get(player.getUUID().toString()).roleType.features.shouldAttack;
        }

        return true;
    }
}
