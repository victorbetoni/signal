package net.threader.signal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventProcessor {
    private Multimap<Class<? extends IEvent>, EventListener> handlers = ArrayListMultimap.create();

    public void register(EventListener listener) {
        Arrays.asList(listener.getListenedEvent()).forEach(x -> {
            handlers.put(x, listener);
        });
    }

    public <E extends IEvent> void post(E event) {
        Optional.of(handlers.get(event.getClass())).ifPresent(handlers -> handlers.forEach(handler ->
                Arrays.stream(handler.getClass().getDeclaredMethods()).filter(method -> method.getParameterTypes().length == 1)
                        .filter(method -> method.getParameterTypes()[0].equals(event.getClass()))
                        .filter(method -> method.getAnnotation(Handler.class) != null)
                        .collect(Collectors.toCollection(HashSet::new)).forEach(method -> {
                    try {
                        method.invoke(handler, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                })));
    }
}
