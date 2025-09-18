package monster.psyop.client.framework.friends.defaults;

import monster.psyop.client.framework.friends.RoleType;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;

import java.awt.*;

public class Neutral extends RoleType {
    public Neutral() {
        super("Neutral", "A literal who?", new ImColorW(new Color(162, 165, 161, 255)));
        this.features.relationship = 1;
        this.features.shouldAttack = true;
        this.features.shouldAnnoy = false;
        this.features.shouldSpam = true;
        this.features.removable = false;
    }
}
