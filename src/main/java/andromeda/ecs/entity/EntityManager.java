package andromeda.ecs.entity;

import andromeda.ecs.system.Signature;

import java.util.*;

public class EntityManager {
    public static int MAX_ENTITIES = 5000;
    public static int MAX_COMPONENTS = 32;

    private Signature[] signatures;

    private Queue<Integer> availableIds;

    private Set<Integer> entities;

    public EntityManager() {
        signatures = new Signature[MAX_ENTITIES];
        availableIds = new LinkedList<>();
        entities = new HashSet<>();
        for (int i = 0; i < MAX_ENTITIES; i++) {
            availableIds.add(i);
        }
    }

    public Set<Integer> getEntities() {
        return entities;
    }

    public Signature getSignature(int entityId) {
        return signatures[entityId];
    }

    public int createEntity() {
        if (!availableIds.isEmpty()) {
            var entityId = availableIds.remove();
            signatures[entityId] = Signature.of();
            entities.add(entityId);
            return entityId;
        } else {
            System.err.println("Maximum number of entities reached!");
            return 0;
        }
    }

    public void destroyEntity(int entityId) {
        if (entityId >= 0 && signatures[entityId] != null) {
            signatures[entityId] = null;
            availableIds.add(entityId);
            entities.remove(entityId);
        }
    }
}
