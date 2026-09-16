package andromeda.ecs.event;

import java.util.*;

public class EventChannel<T extends Event> {

    private final Queue<T> events;
    private final List<EventListener<T>> listeners;

    public EventChannel() {
        events = new LinkedList<>();
        listeners = new ArrayList<>();
    }

    public void subscribe(EventListener<T> eventListener) {
        listeners.add(eventListener);
    }

    public void raise(T event) {
        events.add(event);
    }

    public void trigger() {
        T event;
        while ((event = events.poll()) != null) {
            for (EventListener<T> listener : listeners) {
                listener.consume(event);
            }
        }
    }
}
