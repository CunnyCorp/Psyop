package monster.psyop.client.config.sub;

import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.friends.RoleType;

public class PlayerRole {
    public String uuid;
    public transient RoleType roleType;

    public PlayerRole(String uuid, String roleName) {
        this.uuid = uuid;
        this.roleType = FriendManager.rolesByName.get(roleName);
    }
}
