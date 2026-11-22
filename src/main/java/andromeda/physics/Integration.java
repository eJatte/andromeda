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
        float vel_mag = v.length();
        if (vel_mag > 1e-8) {
            float drag = 0.2f;
            Vector3f drag_force = v.normalize(new Vector3f()).mul(-vel_mag * drag);
            rigidBody.force.add(drag_force);
        }

        Vector3f acceleration = rigidBody.force.mul(rigidBody.inverseMass, new Vector3f());
        acceleration.add(0, -1 * GRAVITY, 0);

        rigidBody.velocity.add(acceleration.mul(DeltaTime.deltaTime));

        transform.translate(rigidBody.velocity.mul(DeltaTime.deltaTime, new Vector3f()));


        Vector3f aV = new Vector3f(rigidBody.angularVelocity);
        float angular_drag = 0.05f;
        // we take the absolute to preserve direction
        Vector3f torque = aV.absolute(new Vector3f()).mul(aV).mul(-angular_drag);
        rigidBody.torque.add(torque);

        Vector3f I = calculateSphericalInvertedInertiaTensor(rigidBody.mass, sphereCollider.radius);
        Vector3f angularAcceleration = rigidBody.torque.mul(I, new Vector3f());

        rigidBody.angularVelocity.add(angularAcceleration.mul(DeltaTime.deltaTime));


        Quaternionf angularQ = new Quaternionf(rigidBody.angularVelocity.x, rigidBody.angularVelocity.y, rigidBody.angularVelocity.z, 0);
        Quaternionf currentQ = transform.getRotation();

        //https://gamedev.stackexchange.com/questions/108920/applying-angular-velocity-to-quaternion
        Quaternionf newQ = angularQ.mul(currentQ).mul(0.5f*DeltaTime.deltaTime).add(currentQ);
        newQ.normalize();
        transform.setRotation(newQ);

        rigidBody.force.zero();
        rigidBody.torque.zero();
    }

    private static Vector3f calculateSphericalInvertedInertiaTensor(float mass, float radius) {
        return new Vector3f(1.0f / (2.0f / 5.0f * mass * radius * radius));
    }
}
