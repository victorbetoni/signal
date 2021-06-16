package net.threader.signal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.threader.signal.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventProcessor {
    private Multimap<Class<?>, Pair<EventListener, Method>> registeredHandlers = ArrayListMultimap.create();
    private Map<Class<? extends Event>, Queue<Pair<EventListener, Method>>> handleQueues = new HashMap<>();

    public void register(EventListener listener) {
        Set<Class<?>> affectedEvents = new HashSet<>();
        Arrays.stream(listener.getClass().getDeclaredMethods())
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0].getSuperclass().equals(Event.class))
                .filter(method -> method.getAnnotation(Handler.class) != null)
                .forEach(method -> {
                    registeredHandlers.put(method.getParameterTypes()[0], new Pair<>(listener, method));
                    affectedEvents.add(method.getParameterTypes()[0]);
                });

        affectedEvents.forEach(x -> {
            Queue<Pair<EventListener, Method>> reorganizedQueue = sort(new HashSet<>(registeredHandlers.get(x)));
            handleQueues.remove(x);
            handleQueues.put((Class<? extends Event>) x, reorganizedQueue);
        });
    }

    public <E extends Event> void post(E event) {
        Optional.ofNullable(handleQueues.get(event.getClass())).ifPresent(queue -> {
            Queue<Pair<EventListener, Method>> copy = new ConcurrentLinkedQueue<>(queue);
            while(!copy.isEmpty()) {
                Pair<EventListener, Method> pair = copy.poll();
                try {
                    pair.getSecond().invoke(pair.getFirst(), event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Queue<Pair<EventListener, Method>> sort(Set<Pair<EventListener, Method>> methods) {
        Queue<Pair<EventListener, Method>> queue = new LinkedBlockingQueue<>();
        methods.stream().filter(t -> t.getSecond().getAnnotation(Handler.class).priority() == Priority.FIRST).forEach(queue::add);
        methods.stream().filter(t -> t.getSecond().getAnnotation(Handler.class).priority() == Priority.HIGH).forEach(queue::add);
        methods.stream().filter(t -> t.getSecond().getAnnotation(Handler.class).priority() == Priority.NORMAL).forEach(queue::add);
        methods.stream().filter(t -> t.getSecond().getAnnotation(Handler.class).priority() == Priority.LOW).forEach(queue::add);
        methods.stream().filter(t -> t.getSecond().getAnnotation(Handler.class).priority() == Priority.LAST).forEach(queue::add);
        return queue;
    }
}
