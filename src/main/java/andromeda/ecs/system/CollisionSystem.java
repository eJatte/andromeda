package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.component.SphereCollider;
import andromeda.ecs.component.Transform;

import java.util.Set;

public class CollisionSystem extends EcsSystem {

    public CollisionSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public void init() {

    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.SPHERE_COLLIDER));
    }

    @Override
    public void update() {
        Integer[] entities = this.getEntities(ComponentType.SPHERE_COLLIDER).toArray(Integer[]::new);
        for(int i = 0; i < entities.length; i++) {
            int e1 = entities[i];
            SphereCollider c1 = ecs.getComponent(SphereCollider.class, e1);
            Transform t1 = ecs.getComponent(Transform.class, e1);
            for(int j = i+1; j < entities.length; j++) {
                int e2 = entities[j];
                SphereCollider c2 = ecs.getComponent(SphereCollider.class, e2);
                Transform t2 = ecs.getComponent(Transform.class, e2);
                float d = t1.getPosition().distance(t2.getPosition());
                if (d < c1.radius + c2.radius) {
                    System.out.println("Collision between " + e1 + " and " + e2);
                }
            }
        }
    }

    @Override
    public SystemType type() {
        return SystemType.CLEANUP;
    }
}
