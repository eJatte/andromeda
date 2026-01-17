package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.ComponentType;

import java.util.*;

public abstract class EcsSystem {
    protected Ecs ecs;
    private Map<Signature, Set<Integer>> entities;

    public EcsSystem(Ecs ecs) {

        this.entities = new HashMap<>();
        for(var signature : getSignatures()) {
            entities.put(signature, new HashSet<>());
        }
        this.ecs = ecs;
    }

    public void addEntity(Signature signature, int entityId) {
        this.entities.get(signature).add(entityId);
    }

    public void removeEntity(int entityId) {
        this.entities.values().forEach(e -> e.remove(entityId));
    }

    public Set<Integer> getEntities(Signature signature) {
        return this.entities.get(signature);
    }

    public Set<Integer> getEntities(ComponentType... componentTypes) {
        return this.getEntities(Signature.of(componentTypes));
    }

    public void init() {
    }

    public void onAdd(int entityId) {}

    public abstract Set<Signature> getSignatures();

    public abstract void update();

    public abstract SystemType type();
}
