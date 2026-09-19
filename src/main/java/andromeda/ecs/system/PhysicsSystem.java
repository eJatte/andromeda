package andromeda.ecs.system;

import andromeda.DeltaTime;
import andromeda.ecs.Ecs;
import andromeda.ecs.component.*;
import andromeda.geometry.Mesh;
import andromeda.geometry.Model;
import andromeda.geometry.Primitives;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.material.Material;
import andromeda.physics.Collision;
import andromeda.physics.CollisionDetection;
import andromeda.physics.Integration;
import andromeda.physics.Solver;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class PhysicsSystem extends EcsSystem {
    public PhysicsSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.TRANSFORM, ComponentType.RIGID_BODY), Signature.of(ComponentType.TRANSFORM, ComponentType.SPHERE_COLLIDER));
    }

    @Override
    public void update() {
        var rigidBodyEntities = this.getEntities(Signature.of(ComponentType.TRANSFORM, ComponentType.RIGID_BODY));
        Integration.integrate(rigidBodyEntities, ecs);

        var colliderEntities = this.getEntities(Signature.of(ComponentType.TRANSFORM, ComponentType.SPHERE_COLLIDER));

        List<Collision> collisions = CollisionDetection.resolveCollisions(colliderEntities, ecs);

        Solver.solve(collisions, ecs);
    }



    @Override
    public SystemType type() {
        return SystemType.PHYSICS;
    }
}
