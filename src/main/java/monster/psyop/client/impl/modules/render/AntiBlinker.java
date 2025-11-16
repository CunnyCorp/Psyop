package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;

public class AntiBlinker extends Module {
    public AntiBlinker() {
        super(Categories.RENDER, "anti-blinker", "Always show every skin part!");
    }
}
