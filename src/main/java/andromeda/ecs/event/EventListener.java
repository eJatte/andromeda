package andromeda.ecs.event;

public interface EventListener<T extends Event> {
    void consume(T event);
}
