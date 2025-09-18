package monster.psyop.client.plugins;

import monster.psyop.client.Liberty;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddonManager {
    private static final String ENTRYPOINT = "psyop";
    public Map<String, JavaAddon> plugins = new HashMap<>();
    public Map<JavaAddon, List<Object>> events = new HashMap<>();

    public void findPlugins() {
        for (EntrypointContainer<?> entrypointContainer :
                FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT, JavaAddon.class)) {
            JavaAddon addon = (JavaAddon) entrypointContainer.getEntrypoint();
            ModMetadata metadata = entrypointContainer.getProvider().getMetadata();
            plugins.put(metadata.getId(), addon);
        }
    }

    public void addEventListener(JavaAddon addon, Object o) {
        Liberty.EVENT_HANDLER.add(o);
        events.computeIfAbsent(addon, v -> new ArrayList<>()).add(o);
    }

    public void enableEvents(JavaAddon addon) {
        for (Object o : events.get(addon)) Liberty.EVENT_HANDLER.enable(o);
    }

    public void disableEvents(JavaAddon addon) {
        for (Object o : events.get(addon)) Liberty.EVENT_HANDLER.disable(o);
    }

    public void removeEvents(JavaAddon addon) {
        for (Object o : events.get(addon)) Liberty.EVENT_HANDLER.remove(o);
    }

    public void initialize() {
        long ns = System.nanoTime();

        FabricLoader.getInstance().invokeEntrypoints(ENTRYPOINT, JavaAddon.class, JavaAddon::onInit);

        Liberty.log("Took {} nanoseconds to init plugins.", System.nanoTime() - ns);
    }
}
