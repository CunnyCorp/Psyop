package monster.psyop.client.framework.modules;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import monster.psyop.client.Psyop;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Modules {
    protected final Map<Category, List<Module>> MODULES = new Object2ObjectArrayMap<>();
    protected final Map<String, Module> NAME_TO_MODULE = new Object2ObjectArrayMap<>();
    protected final Map<Class<? extends Module>, Module> CLASS_TO_MODULE =
            new Object2ObjectArrayMap<>();

    public Modules() {
        for (Category category : Categories.INDEX) {
            MODULES.putIfAbsent(category, new ObjectArrayList<>());
        }
    }

    public List<Module> getModules(Category category) {
        return MODULES.get(category);
    }

    public void add(Module... modules) {
        if (modules == null) {
            Psyop.error("NO ! DUCKS FLIGHTLESS !");
            return;
        }

        for (Module module : modules) {
            if (module == null) {
                Psyop.error("DUCKS CANNOT FLY ! ! !");
                continue;
            }

            List<Module> moduleSet = MODULES.get(module.category);

            if (moduleSet.contains(module) || NAME_TO_MODULE.containsKey(module.name)) {
                Psyop.warn("Module {} has a duplicate.", module.name);
                continue;
            }

            moduleSet.add(module);
            NAME_TO_MODULE.put(module.name, module);
            CLASS_TO_MODULE.put(module.getClass(), module);

            Psyop.log("Module {} was successfully loaded.", module.name);

            module.active(false);
        }
    }

    public boolean exists(Class<? extends Module> clazz) {
        return CLASS_TO_MODULE.containsKey(clazz);
    }

    public boolean exists(String name) {
        return NAME_TO_MODULE.containsKey(name);
    }

    public List<Module> getCategory(Category category) {
        return MODULES.get(category);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> clazz) {
        return (T) CLASS_TO_MODULE.get(clazz);
    }

    @Nullable
    public Module get(String name) {
        return NAME_TO_MODULE.get(name);
    }

    public boolean isActive(Class<? extends Module> clazz) {
        return exists(clazz) && get(clazz).active();
    }
}
