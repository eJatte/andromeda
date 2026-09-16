package andromeda.ecs.event;

public enum EventType {
    COLLISION(1);

    public final int id;

    EventType(int id) {
        this.id = id;
    }
}
