package andromeda.ecs.component;

public class SphereCollider implements Component {

    public float radius = 1;

    @Override
    public ComponentType componentType() {
        return ComponentType.SphereCollider;
    }

    @Override
    public Component createComponent() {
        return new SphereCollider();
    }

    @Override
    public Component copy() {
        SphereCollider copy = new SphereCollider();
        copy.radius = this.radius;
        return copy;
    }
}
