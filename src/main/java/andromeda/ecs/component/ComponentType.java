package andromeda.ecs.component;

public enum ComponentType {
    TRANSFORM(1),
    MODEL(2),
    HIERACHY(3),
    POINT_LIGHT(4),
    DIRECTIONAL_LIGHT(5),
    CAMERA(6),
    FPS_CONTROL(7),
    RIGID_BODY(8),
    PERSPECTIVE(9),
    DEATH_TIMER(10);

    public final int id;

    ComponentType(int id) {
        this.id = id;
    }
}
