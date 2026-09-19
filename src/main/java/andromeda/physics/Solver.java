package andromeda.physics;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.RigidBody;
import andromeda.ecs.component.Transform;
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
        if(collision.entity_b() == -1) {
            Transform t_a = ecs.getComponent(Transform.class, collision.entity_a());
            RigidBody r_a = ecs.getComponent(RigidBody.class, collision.entity_a());
            if (r_a == null) {
                return;
            }

            float inverse_mass_tot = r_a.inverseMass;

            Vector3f correction = collision.normal().mul(collision.depth() / inverse_mass_tot, new Vector3f());

            t_a.translate(correction.mul(r_a.inverseMass, new Vector3f()).negate());
            r_a.velocity.zero();

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

        // TODO make impulse based
        r_a.velocity.zero();
        r_b.velocity.zero();
    }
}
