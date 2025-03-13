package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.entity.EntityManager;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class EcsSystem {
    private BitSet signature;
    EcsCoordinator ecsCoordinator;
    Set<Integer> entities;

    public EcsSystem(List<ComponentType> signature, EcsCoordinator ecsCoordinator) {
        this.signature = new BitSet(EntityManager.MAX_COMPONENTS);
        for (ComponentType componentType : signature) {
            this.signature.set(componentType.id, true);
        }

        this.entities = new HashSet<>();
        this.ecsCoordinator = ecsCoordinator;
    }

    public BitSet getSignature() {
        return signature;
    }

    public void addEntity(int entityId) {
        this.entities.add(entityId);
    }

    public void removeEntity(int entityId) {
        this.entities.remove(entityId);
    }

    public void init() {
    }

    public abstract void update();

    public abstract SystemType type();
}
