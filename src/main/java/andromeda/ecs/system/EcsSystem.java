package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.entity.EntityManager;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class EcsSystem {
    private final Set<BitSet> signatures;
    EcsCoordinator ecsCoordinator;
    Set<Integer> entities;

    public EcsSystem(List<List<ComponentType>> signatures, EcsCoordinator ecsCoordinator) {
        this.signatures = new HashSet<>();
        for (var signature : signatures) {
            var bitset = new BitSet(EntityManager.MAX_COMPONENTS);
            for (ComponentType componentType : signature) {
                bitset.set(componentType.id, true);
            }
            this.signatures.add(bitset);
        }

        this.entities = new HashSet<>();
        this.ecsCoordinator = ecsCoordinator;
    }

    public Set<BitSet> getSignature() {
        return signatures;
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
