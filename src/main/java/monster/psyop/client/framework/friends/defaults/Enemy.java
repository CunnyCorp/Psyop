package monster.psyop.client.framework.friends.defaults;

import monster.psyop.client.framework.friends.RoleType;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;

import java.awt.*;

public class Enemy extends RoleType {
    public Enemy() {
        super("Enemy", "Frick these guys, am I right?", new ImColorW(new Color(230, 70, 100, 255)));
        this.features.relationship = 0;
        this.features.shouldAttack = true;
        this.features.shouldAnnoy = true;
        this.features.shouldSpam = true;
        this.features.removable = false;
    }
}
