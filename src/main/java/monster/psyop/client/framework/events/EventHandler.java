package monster.psyop.client.framework.events;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.lang.reflect.Method;
import java.util.Comparator;

import static monster.psyop.client.Psyop.MC;

/**
 * The type Event handler.
 */
public class EventHandler {
    private final Object2ObjectArrayMap<Class<?>, ObjectArrayList<EventWrapper>> eventMap = new Object2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<Method, EventWrapper> methodWrappers = new Object2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<Object, ObjectArrayList<EventWrapper>> disabled = new Object2ObjectArrayMap<>();

    /**
     * Assigns an objects methods to events.
     *
     * @param eventObject an object with an event-listener.
     */
    public void add(Object eventObject) {
        for (Method method : eventObject.getClass().getDeclaredMethods()) {

            if (!method.isAnnotationPresent(EventListener.class)) {
                continue;
            }

            EventListener eventListener = method.getAnnotation(EventListener.class);

            Class<?> clazz = method.getParameterTypes()[0];

            mkEventMap(clazz);

            method.setAccessible(true);

            EventWrapper wrapper = new EventWrapper(eventObject, eventListener, method);

            wrapper.setId(clazz);

            methodWrappers.put(method, wrapper);

            ObjectArrayList<EventWrapper> wrappers = eventMap.get(clazz);

            wrappers.add(wrapper);

            if (wrappers.size() > 1 && wrappers.get(wrappers.size() - 1).event().priority() < methodWrappers.get(method).event().priority()) {
                wrappers.sort(Comparator.comparingInt((c1) -> c1.event().priority()));
            }
        }
    }

    public void call(Event event) {
        call0(event);
        //this.executorService.execute(() -> call0(event));
    }

    /**
     * Invoke methods with this event, will also break the loop if needed.
     *
     * @param event The event to distribute calls to.
     */
    public void call0(Event event) {

        Class<? extends Event> clazz = event.getClass();

        if (!eventMap.containsKey(clazz)) {
            return;
        }

        boolean inGame = MC.player != null && MC.level != null;

        for (EventWrapper wrapper : eventMap.get(clazz)) {
            if (wrapper.event().inGame() && !inGame) {
                continue;
            }

            try {
                wrapper.methodHandle().invoke(wrapper.getObject(), event);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (event.isCancelled() && wrapper.event().breakOnCancel()) {
                break;
            }
        }
    }

    /**
     * Disable an objects events.
     *
     * @param eventObject the object with events
     */
    public void disable(Object eventObject) {
        if (disabled.containsKey(eventObject)) return;
        disabled.put(eventObject, new ObjectArrayList<>());
        for (Method method : eventObject.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventListener.class)) {
                continue;
            }
            disabled.get(eventObject).add(methodWrappers.get(method));
        }

        for (Class<?> clazz : eventMap.keySet()) {
            eventMap.get(clazz).removeIf(wrapper -> disabled.get(eventObject).contains(wrapper));
        }
    }

    /**
     * Enable an objects events.
     *
     * @param eventObject the object with events
     */
    public void enable(Object eventObject) {
        if (!disabled.containsKey(eventObject)) return;

        for (EventWrapper wrapper : disabled.get(eventObject)) {
            mkEventMap(wrapper.id());
            eventMap.get(wrapper.id()).add(wrapper);
        }

        disabled.remove(eventObject);
    }

    /**
     * Remove an objects events.
     *
     * @param eventObject the object with events
     */
    public void remove(Object eventObject) {
        disabled.remove(eventObject);

        for (Method method : eventObject.getClass().getDeclaredMethods()) {
            if (methodWrappers.containsKey(method)) {
                eventMap.get(methodWrappers.get(method).id()).remove(methodWrappers.get(method));
            }
        }
    }

    private void mkEventMap(Class<?> event) {
        if (!eventMap.containsKey(event)) eventMap.put(event, new ObjectArrayList<>());
    }
}
