package net.threader.signal;

public abstract class EventListener {
    private Class<? extends IEvent>[] listenedEvents;

    @SafeVarargs
    public EventListener(Class<? extends IEvent>... listenedEvents) {
        this.listenedEvents = listenedEvents;
    }

    public Class<? extends IEvent>[] getListenedEvent() {
        return listenedEvents;
    }
}
