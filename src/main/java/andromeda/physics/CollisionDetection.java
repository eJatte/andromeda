package andromeda.physics;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.SphereCollider;
import andromeda.ecs.component.Transform;
import andromeda.ecs.event.CollisionEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CollisionDetection {

    public static List<Collision> resolveCollisions(Set<Integer> entities, Ecs ecs) {
        List<Collision> collisions = new ArrayList<>();
        Integer[] e = entities.toArray(Integer[]::new);

        for (int i = 0; i < e.length; i++) {
            int e1 = e[i];
            SphereCollider c1 = ecs.getComponent(SphereCollider.class, e1);
            Transform t1 = ecs.getComponent(Transform.class, e1);
            for (int j = i + 1; j < e.length; j++) {
                int e2 = e[j];
                SphereCollider c2 = ecs.getComponent(SphereCollider.class, e2);
                Transform t2 = ecs.getComponent(Transform.class, e2);
                var collison = resolveCollisionSphereSphere(c1, t1, c2, t2, e1, e2);
                collison.ifPresent(collisions::add);
            }
            float diff = -t1.getPosition().y;
            float distance = Math.abs(diff);
            float depth = distance - c1.radius;

            if(depth <= 0) {
                Vector3f normal = new Vector3f(0, diff < 0 ? -1 : 1, 0);
                collisions.add(new Collision(normal, Math.abs(depth), e1, -1));
            }
        }

        return collisions;
    }

    private static Optional<Collision> resolveCollisionSphereSphere(SphereCollider c_a, Transform t_a, SphereCollider c_b, Transform t_b, int e_a, int e_b) {
        Vector3f diff = t_b.getPosition().sub(t_a.getPosition(), new Vector3f());

        float distance = diff.length();
        float depth = distance - (c_a.radius + c_b.radius);

        return depth <= 0 ? Optional.of(new Collision(diff.normalize(), Math.abs(depth), e_a, e_b)) : Optional.empty();
    }
}
