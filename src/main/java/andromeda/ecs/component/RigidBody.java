package andromeda.ecs.component;

import org.joml.Vector3f;

public class RigidBody implements Component {
    public Vector3f velocity = new Vector3f(0);
    public float mass = 1;
    public float inverseMass = 1;
    public Vector3f force = new Vector3f(0);
    public float drag = 0.01f;

    @Override
    public ComponentType componentType() {
        return ComponentType.RIGID_BODY;
    }

    @Override
    public Component createComponent() {
        return new RigidBody();
    }

    public void setMass(float mass) {
        this.mass = mass;
        this.inverseMass = 1 / mass;
    }

    @Override
    public Component copy() {
        var comp = new RigidBody();
        comp.velocity = new Vector3f(this.velocity);
        comp.drag = this.drag;
        return comp;
    }
}
