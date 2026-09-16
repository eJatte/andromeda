package andromeda.ecs.event;


import java.util.*;

public class EventManager {
    private Map<Class<?>, EventChannel<?>> eventChannels;


    public EventManager() {
        eventChannels = new HashMap<>();
    }

    public <T extends Event> void registerEvent(T event) {
        if (eventChannels.containsKey(event.getClass())) {
            throw new IllegalStateException("Event type already registered");
        }
        eventChannels.put(event.getClass(), new EventChannel<T>());
    }

    public <T extends Event> void registerListener(Class<T> clazz, EventListener<T> listener) {
        getEventChannel(clazz).subscribe(listener);
    }

    public <T extends Event> void raiseEvent(Class<T> clazz, T event) {
        getEventChannel(clazz).raise(event);
    }

    public void triggerEvents() {
        for (EventChannel<?> channel : eventChannels.values()) {
            channel.trigger();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> EventChannel<T> getEventChannel(Class<T> clazz) {
        if (!eventChannels.containsKey(clazz)) {
            throw new IllegalStateException("Event type not registered " + clazz.getName());
        }

        return (EventChannel<T>) eventChannels.get(clazz);
    }
}
