package andromeda.ecs.component;

public enum ComponentType {
    TRANSFORM(1),
    MODEL(2),
    HIERACHY(3),
    POINT_LIGHT(4),
    DIRECTIONAL_LIGHT(5),
    CAMERA(6);

    public final int id;

    ComponentType(int id) {
        this.id = id;
    }
}
