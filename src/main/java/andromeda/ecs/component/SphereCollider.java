package andromeda.ecs.component;

public class SphereCollider implements Component {

    public float radius = 1;
    public boolean debug = false;

    @Override
    public ComponentType componentType() {
        return ComponentType.SPHERE_COLLIDER;
    }

    @Override
    public Component createComponent() {
        return new SphereCollider();
    }

    @Override
    public Component copy() {
        SphereCollider copy = new SphereCollider();
        copy.radius = this.radius;
        copy.debug = this.debug;
        return copy;
    }
}
