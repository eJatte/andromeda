package andromeda.physics;

import andromeda.DeltaTime;
import andromeda.ecs.Ecs;
import andromeda.ecs.component.RigidBody;
import andromeda.ecs.component.Transform;
import org.joml.Vector3f;

import java.util.Set;

public class Integration {

    private static final float GRAVITY = 9.82f;

    public static void integrate(Set<Integer> entities, Ecs ecs) {
        entities.forEach(e -> integrate(e, ecs));
    }

    private static void integrate(int entityId, Ecs ecs) {
        Transform transform = ecs.getComponent(Transform.class, entityId);
        RigidBody rigidBody = ecs.getComponent(RigidBody.class, entityId);

        Vector3f acceleration = rigidBody.force.mul(rigidBody.inverseMass, new Vector3f());
        acceleration.add(0, -1 * GRAVITY, 0);

        rigidBody.velocity.add(acceleration.mul(DeltaTime.deltaTime));

        transform.translate(rigidBody.velocity.mul(DeltaTime.deltaTime, new Vector3f()));

        rigidBody.force.zero();
    }
}
