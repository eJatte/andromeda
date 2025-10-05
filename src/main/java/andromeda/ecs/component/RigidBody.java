package andromeda.ecs.component;

import org.joml.Vector3f;

public class RigidBody implements Component {
    public Vector3f velocity = new Vector3f(0);
    public float drag = 0.01f;

    @Override
    public ComponentType componentType() {
        return ComponentType.RIGID_BODY;
    }

    @Override
    public Component createComponent() {
        return new RigidBody();
    }
}
