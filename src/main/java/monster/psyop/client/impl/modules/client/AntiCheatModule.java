package monster.psyop.client.impl.modules.client;

import imgui.type.ImString;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.ProvidedStringSetting;

import java.util.List;

public class AntiCheatModule extends Module {
    public ProvidedStringSetting antiCheat = new ProvidedStringSetting.Builder()
            .suggestions(List.of(new ImString("Universal"), new ImString("Veck_Grim"), new ImString("Fraze_Grim")))
            .name("anti-cheat")
            .defaultTo(new ImString("Universal"))
            .addTo(coreGroup);

    public AntiCheatModule() {
        super(Categories.CLIENT, "anti-cheat", "Let's you hide modules depending on the anti-cheat they're made for.");
    }
}
