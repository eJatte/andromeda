package andromeda.ecs.system;

import andromeda.ecs.Ecs;

import java.util.*;

public abstract class EcsSystem {
    Ecs ecs;
    Set<Integer> entities;

    public EcsSystem(Ecs ecs) {
        this.entities = new HashSet<>();
        this.ecs = ecs;
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
