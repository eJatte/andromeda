package andromeda.physics;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.RigidBody;
import andromeda.ecs.component.SphereCollider;
import andromeda.ecs.component.Transform;
import andromeda.ecs.system.DebugSystem;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class Solver {

    public static void solve(List<Collision> collisions, Ecs ecs) {
        resolveOverlaps(collisions, ecs);
    }

    private static void resolveOverlaps(List<Collision> collisions, Ecs ecs) {
        collisions.forEach(c -> resolveOverlap(c, ecs));
    }

    private static void resolveOverlap(Collision collision, Ecs ecs) {
        if (collision.entity_b() == -1) {
            Transform t_a = ecs.getComponent(Transform.class, collision.entity_a());
            RigidBody r_a = ecs.getComponent(RigidBody.class, collision.entity_a());
            if (r_a == null) {
                return;
            }

            float inverse_mass_tot = r_a.inverseMass;

            Vector3f correction = collision.normal().mul(collision.depth() / inverse_mass_tot, new Vector3f());

            t_a.translate(correction.mul(r_a.inverseMass, new Vector3f()).negate());

            Vector3f j = impulseFloor(collision, ecs);

            r_a.velocity.sub(j.mul(r_a.inverseMass));

            return;
        }

        Transform t_a = ecs.getComponent(Transform.class, collision.entity_a());
        RigidBody r_a = ecs.getComponent(RigidBody.class, collision.entity_a());

        Transform t_b = ecs.getComponent(Transform.class, collision.entity_b());
        RigidBody r_b = ecs.getComponent(RigidBody.class, collision.entity_b());

        if (r_a == null || r_b == null) {
            return;
        }

        float inverse_mass_tot = r_a.inverseMass + r_b.inverseMass;

        Vector3f correction = collision.normal().mul(collision.depth() / inverse_mass_tot, new Vector3f());

        t_a.translate(correction.mul(r_a.inverseMass, new Vector3f()).negate());
        t_b.translate(correction.mul(r_b.inverseMass, new Vector3f()));

        Vector3f j = impulse(collision, ecs);

        r_a.velocity.sub(j.mul(r_a.inverseMass));
        r_b.velocity.add(j.mul(r_b.inverseMass));
    }

    private static Vector3f impulse(Collision collision, Ecs ecs) {
        Transform t_a = ecs.getComponent(Transform.class, collision.entity_a());
        RigidBody r_a = ecs.getComponent(RigidBody.class, collision.entity_a());
        SphereCollider s_a = ecs.getComponent(SphereCollider.class, collision.entity_a());
        Transform t_b = ecs.getComponent(Transform.class, collision.entity_b());
        RigidBody r_b = ecs.getComponent(RigidBody.class, collision.entity_b());
        SphereCollider s_b = ecs.getComponent(SphereCollider.class, collision.entity_b());
        // restitution calculate based on min(rest1, rest2) value between 0-1. Determines of elastic the collision is.
        // for now we hardcode it
        float e = 1;
        Vector3f I_a = calculateSphericalInertiaTensor(r_a.mass, s_a.radius);
        Vector3f I_b = calculateSphericalInertiaTensor(r_b.mass, s_b.radius);

        Vector3f rv_a = collision.contactPoint().sub(t_a.getPosition(), new Vector3f());
        Vector3f rv_b = collision.contactPoint().sub(t_b.getPosition(), new Vector3f());

        Vector3f v_r = r_b.velocity.sub(r_a.velocity, new Vector3f());

        Vector3f n = collision.normal();

        float m_ai = r_a.inverseMass;
        float m_bi = r_b.inverseMass;

        float j = (-(1 + e) * v_r.dot(n)) / (m_ai + m_bi + (f(I_a, rv_a, n).add(f(I_b, rv_b, n))).dot(n));

        return n.mul(j, new Vector3f());
    }

    private static Vector3f impulseFloor(Collision collision, Ecs ecs) {
        Transform t_a = ecs.getComponent(Transform.class, collision.entity_a());
        RigidBody r_a = ecs.getComponent(RigidBody.class, collision.entity_a());
        SphereCollider s_a = ecs.getComponent(SphereCollider.class, collision.entity_a());
        // restitution calculate based on min(rest1, rest2) value between 0-1. Determines of elastic the collision is.
        // for now we hardcode it
        float e = 1;
        Vector3f I_a = calculateSphericalInertiaTensor(r_a.mass, s_a.radius);

        Vector3f rv_a = collision.contactPoint().sub(t_a.getPosition(), new Vector3f());

        Vector3f v_r = new Vector3f().sub(r_a.velocity, new Vector3f());

        Vector3f n = collision.normal();

        float m_ai = r_a.inverseMass;

        float j = (-(1 + e) * v_r.dot(n)) / (m_ai + (f(I_a, rv_a, n)).dot(n));

        return n.mul(j, new Vector3f());
    }

    private static Vector3f f(Vector3f tensor, Vector3f r, Vector3f n) {
        return tensor.mul(r.cross(n).cross(r));
    }

    private static Vector3f calculateSphericalInertiaTensor(float mass, float radius) {
        return new Vector3f(2.0f / 5.0f * mass * radius * radius);
    }
}
