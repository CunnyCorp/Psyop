package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;

public class AntiPush extends Module {
    public final BoolSetting hooking =
            new BoolSetting.Builder()
                    .name("hooking")
                    .description("Modifies hooking pull.")
                    .defaultTo(false)
                    .addTo(coreGroup);


    public AntiPush() {
        super(Categories.MOVEMENT, "anti-push", "Prevents mobs from pushing you.");
    }
}
