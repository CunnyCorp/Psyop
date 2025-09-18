package monster.psyop.client.framework.gui.hud;

import imgui.type.ImBoolean;
import monster.psyop.client.config.Config;

import java.util.*;

public class HudHandler {
    protected static final Map<Class<? extends HudElement>, ImBoolean> ELEMENT_STATES = new HashMap<>();
    protected static final Map<Class<? extends HudElement>, HudElement> ELEMENTS = new HashMap<>();
    protected static final List<HudElement> SORTED_ELEMENTS = new ArrayList<>();


    public static void add(HudElement element) {
        ELEMENTS.put(element.getClass(), element);
        SORTED_ELEMENTS.add(element);
        if (SORTED_ELEMENTS.size() >= 2) {
            SORTED_ELEMENTS.sort(Comparator.comparing(HudElement::name));
        }
        element.populateSettings(Config.get());
        ELEMENT_STATES.put(element.getClass(), element.settings.isLoaded);
    }

    public static ImBoolean state(Class<? extends HudElement> element) {
        ELEMENT_STATES.putIfAbsent(element, new ImBoolean(false));
        return ELEMENT_STATES.get(element);
    }

    public static <T extends HudElement> T get(Class<T> clazz) {
        return (T) ELEMENTS.get(clazz);
    }

    public static Collection<HudElement> getElements() {
        return SORTED_ELEMENTS;
    }

    public static void showAll() {
        for (HudElement element : SORTED_ELEMENTS) {
            if (element.shouldShow()) {
                element.show();
            }
        }
    }
}
