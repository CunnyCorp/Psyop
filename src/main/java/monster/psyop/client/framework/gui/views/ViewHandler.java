package monster.psyop.client.framework.gui.views;

import imgui.type.ImBoolean;
import monster.psyop.client.config.Config;
import monster.psyop.client.impl.modules.client.GUIModule;

import java.util.*;

public class ViewHandler {
    protected static final Map<Class<? extends View>, ImBoolean> VIEW_STATES = new HashMap<>();
    protected static final Map<Class<? extends View>, View> VIEWS = new HashMap<>();
    protected static final List<View> SORTED_VIEWS = new ArrayList<>();


    public static void add(View view) {
        VIEWS.put(view.getClass(), view);
        SORTED_VIEWS.add(view);
        if (SORTED_VIEWS.size() >= 2) {
            SORTED_VIEWS.sort(Comparator.comparing(View::name));
        }
        view.populateSettings(Config.get());
        VIEW_STATES.put(view.getClass(), view.settings.isLoaded);
    }

    public static ImBoolean state(Class<? extends View> view) {
        VIEW_STATES.putIfAbsent(view, new ImBoolean(false));
        return VIEW_STATES.get(view);
    }

    public static <T extends View> T get(Class<T> clazz) {
        return (T) VIEWS.get(clazz);
    }

    public static Collection<View> getViews() {
        return SORTED_VIEWS;
    }

    public static void showAll() {
        GUIModule.INSTANCE.applyStyleColors();
        for (View view : SORTED_VIEWS) {
            if (state(view.getClass()).get()) {
                view.show();
            }
        }
    }
}
