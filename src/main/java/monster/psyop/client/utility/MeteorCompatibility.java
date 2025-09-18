package monster.psyop.client.utility;

import meteordevelopment.meteorclient.gui.WidgetScreen;
import monster.psyop.client.Liberty;

public class MeteorCompatibility {
    public static boolean isMeteorRendering() {
        return Liberty.MC.screen instanceof WidgetScreen;
    }
}
