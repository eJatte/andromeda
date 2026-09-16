package andromeda.ecs.event;

public class CollisionEvent implements Event {

    public int entity_a = -1, entity_b = -1;

    @Override
    public EventType eventType() {
        return EventType.COLLISION;
    }
}
