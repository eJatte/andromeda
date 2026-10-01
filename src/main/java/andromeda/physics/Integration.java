package andromeda.physics;

import andromeda.DeltaTime;
import andromeda.ecs.Ecs;
import andromeda.ecs.component.RigidBody;
import andromeda.ecs.component.SphereCollider;
import andromeda.ecs.component.Transform;
import org.joml.Quaternionf;
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
        SphereCollider sphereCollider = ecs.getComponent(SphereCollider.class, entityId);


        // https://en.wikipedia.org/wiki/Drag_(physics)
        Vector3f v = new Vector3f(rigidBody.velocity);
        float drag_coef = 0.3f;
        // we take the absolute to preserve direction
        Vector3f drag_force = v.absolute(new Vector3f()).mul(v).mul(-drag_coef);
        rigidBody.force.add(drag_force);

        Vector3f acceleration = rigidBody.force.mul(rigidBody.inverseMass, new Vector3f());
        acceleration.add(0, -1 * GRAVITY, 0);

        rigidBody.velocity.add(acceleration.mul(DeltaTime.deltaTime));

        transform.translate(rigidBody.velocity.mul(DeltaTime.deltaTime, new Vector3f()));

        Quaternionf angularQ = new Quaternionf(rigidBody.angularVelocity.x, rigidBody.angularVelocity.y, rigidBody.angularVelocity.z, 0);
        Quaternionf currentQ = transform.getRotation();

        //https://gamedev.stackexchange.com/questions/108920/applying-angular-velocity-to-quaternion
        Quaternionf newQ = angularQ.mul(currentQ).mul(0.5f*DeltaTime.deltaTime).add(currentQ);
        newQ.normalize();
        transform.setRotation(newQ);

        rigidBody.force.zero();
    }
}
