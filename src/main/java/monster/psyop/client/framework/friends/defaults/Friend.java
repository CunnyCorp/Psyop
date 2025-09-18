package monster.psyop.client.framework.friends.defaults;

import monster.psyop.client.framework.friends.RoleType;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;

import java.awt.*;

public class Friend extends RoleType {
    public Friend() {
        super("Friend", "Friendly and no messing with em!", new ImColorW(new Color(203, 130, 244, 255)));
        this.features.relationship = 2;
        this.features.shouldAttack = false;
        this.features.shouldAnnoy = false;
        this.features.shouldSpam = false;
        this.features.removable = false;
    }
}
