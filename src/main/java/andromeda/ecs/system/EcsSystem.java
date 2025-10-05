package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.entity.EntityManager;

import java.util.*;

public abstract class EcsSystem {
    EcsCoordinator ecsCoordinator;
    Set<Integer> entities;

    public EcsSystem(EcsCoordinator ecsCoordinator) {
        this.entities = new HashSet<>();
        this.ecsCoordinator = ecsCoordinator;
    }

    public void addEntity(int entityId) {
        this.entities.add(entityId);
    }

    public void removeEntity(int entityId) {
        this.entities.remove(entityId);
    }

    public void init() {
    }

    public abstract Set<ComponentSignature> getSignatures();

    public abstract void update();

    public abstract SystemType type();
}
