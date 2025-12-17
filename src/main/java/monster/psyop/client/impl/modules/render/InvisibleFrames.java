package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;

public class InvisibleFrames extends Module {
    public InvisibleFrames() {
        super(Categories.RENDER, "invisible-frames", "Makes item frames backgrounds transparent when they have an item in them.");
    }
}
