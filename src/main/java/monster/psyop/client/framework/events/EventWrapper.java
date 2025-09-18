package monster.psyop.client.framework.events;

import lombok.Getter;
import lombok.Setter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class EventWrapper {
    @Getter
    private final Object object;
    private final EventListener event;
    private final Method method;
    private final MethodHandle methodHandle;
    @Setter
    private Class<?> id;

    public EventWrapper(Object object, EventListener event, Method method) {
        this.object = object;
        this.event = event;

        try {
            MethodHandles.Lookup lookup =
                    MethodHandles.privateLookupIn(object.getClass(), MethodHandles.lookup());
            this.methodHandle = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        this.method = method;
    }

    public Class<?> id() {
        return id;
    }

    public EventListener event() {
        return event;
    }

    public Method method() {
        return method;
    }

    public MethodHandle methodHandle() {
        return methodHandle;
    }
}
