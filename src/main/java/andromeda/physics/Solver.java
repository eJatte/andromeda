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

            impulseFloor(collision, ecs);

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


        applyImpulse(collision, ecs);
    }

    private static void applyImpulse(Collision collision, Ecs ecs) {
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

        Vector3f v_a = r_a.angularVelocity.cross(rv_a, new Vector3f()).add(r_a.velocity);
        Vector3f v_b = r_b.angularVelocity.cross(rv_b, new Vector3f()).add(r_b.velocity);

        Vector3f v_r = v_b.sub(v_a, new Vector3f());

        Vector3f n = collision.normal();

        float m_ai = r_a.inverseMass;
        float m_bi = r_b.inverseMass;

        float j_r_s = (-(1 + e) * v_r.dot(n)) / (m_ai + m_bi + (f(I_a, rv_a, n).add(f(I_b, rv_b, n))).dot(n));

        Vector3f j_r = n.mul(j_r_s, new Vector3f());

        r_a.velocity.sub(j_r.mul(r_a.inverseMass, new Vector3f()));
        r_b.velocity.add(j_r.mul(r_b.inverseMass, new Vector3f()));

        r_a.angularVelocity.sub(calculateAngularVelocity(I_a, rv_a, n).mul(j_r_s));
        r_b.angularVelocity.add(calculateAngularVelocity(I_b, rv_b, n).mul(j_r_s));

        float u_s = 0.6f;
        float u_d = 0.4f;

        Vector3f t = new Vector3f(0);
        float dot = v_r.dot(n);
        if (dot != 0) {
            t = v_r.sub(n.mul(dot, new Vector3f()), new Vector3f());
            if (t.length() != 0) {
                t.normalize();
            }
        }

        float j_s = u_s*j_r_s;
        float j_d = u_d*j_r_s;

        Vector3f j_f;
        float v_dot_t = v_r.dot(t);
        float what = v_dot_t*r_a.mass;
        if(v_dot_t == 0 && what <= j_s) {
            j_f = t.mul(-what, new Vector3f());
        }
        else {
            j_f = t.mul(-j_d, new Vector3f());
        }

        r_a.velocity.sub(j_f.mul(r_a.inverseMass, new Vector3f()));
        r_b.velocity.add(j_f.mul(r_b.inverseMass, new Vector3f()));

        r_a.angularVelocity.sub(calculateAngularVelocity(I_a, rv_a, n).mul(j_f.length()));
        r_b.angularVelocity.add(calculateAngularVelocity(I_b, rv_b, n).mul(j_f.length()));
    }

    private static Vector3f calculateAngularVelocity(Vector3f tensor, Vector3f r, Vector3f n) {
        return tensor.mul(r.cross(n, new Vector3f()), new Vector3f());
    }

    private static void impulseFloor(Collision collision, Ecs ecs) {
        Transform t_a = ecs.getComponent(Transform.class, collision.entity_a());
        RigidBody r_a = ecs.getComponent(RigidBody.class, collision.entity_a());
        SphereCollider s_a = ecs.getComponent(SphereCollider.class, collision.entity_a());
        // restitution calculate based on min(rest1, rest2) value between 0-1. Determines of elastic the collision is.
        // for now we hardcode it
        float e = 1;
        Vector3f I_a = calculateSphericalInertiaTensor(r_a.mass, s_a.radius);

        Vector3f rv_a = collision.contactPoint().sub(t_a.getPosition(), new Vector3f());

        Vector3f v_a = r_a.angularVelocity.cross(rv_a, new Vector3f()).add(r_a.velocity);

        Vector3f v_r = new Vector3f().sub(v_a, new Vector3f());

        Vector3f n = collision.normal();

        float m_ai = r_a.inverseMass;

        float j_r_s = (-(1 + e) * v_r.dot(n)) / (m_ai + (f(I_a, rv_a, n)).dot(n));

        Vector3f j_r = n.mul(j_r_s, new Vector3f());

        r_a.velocity.sub(j_r.mul(r_a.inverseMass, new Vector3f()));

        r_a.angularVelocity.sub(calculateAngularVelocity(I_a, rv_a, n).mul(j_r_s));



        float u_s = 0.6f;
        float u_d = 0.4f;

        Vector3f t = new Vector3f(0);
        float dot = v_r.dot(n);
        if (dot != 0) {
            t = v_r.sub(n.mul(dot, new Vector3f()), new Vector3f());
            if (t.length() != 0) {
                t.normalize();
            }
        }

        float j_s = u_s*j_r_s;
        float j_d = u_d*j_r_s;

        Vector3f j_f;
        float v_dot_t = v_r.dot(t);
        float what = v_dot_t*r_a.mass;
        if(v_dot_t == 0 && what <= j_s) {
            j_f = t.mul(-what, new Vector3f());
        }
        else {
            j_f = t.mul(-j_d, new Vector3f());
        }

        r_a.velocity.sub(j_f.mul(r_a.inverseMass, new Vector3f()));

        r_a.angularVelocity.sub(calculateAngularVelocity(I_a, rv_a, n).mul(j_f.length()));
    }

    private static Vector3f f(Vector3f tensor, Vector3f r, Vector3f n) {
        return tensor.mul(r.cross(n, new Vector3f()).cross(r, new Vector3f()), new Vector3f());
    }

    private static Vector3f calculateSphericalInertiaTensor(float mass, float radius) {
        return new Vector3f(2.0f / 5.0f * mass * radius * radius);
    }
}
