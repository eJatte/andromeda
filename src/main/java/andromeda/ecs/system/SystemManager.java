package andromeda.ecs.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemManager {
    private Map<Class<?>, EcsSystem> systems;

    public SystemManager() {
        this.systems = new HashMap<>();
    }

    public void entitySignatureUpdate(int entityId, Signature signature) {
        for (EcsSystem ecsSystem : systems.values()) {
            List<Signature> matchingSignatures = ecsSystem.getSignatures().stream().filter(signature::contains).toList();

            if (matchingSignatures.isEmpty()) {
                ecsSystem.removeEntity(entityId);
            } else {
                matchingSignatures.forEach(s -> ecsSystem.addEntity(s, entityId));
            }
        }
    }

    public void entityDestroyed(int entityId) {
        for (EcsSystem ecsSystem : systems.values()) {
            ecsSystem.removeEntity(entityId);
        }
    }

    public <T extends EcsSystem> void registerSystem(T system) {
        systems.put(system.getClass(), system);
    }

    public <T extends EcsSystem> T getSystem(Class<T> clazz) {
        if (!systems.containsKey(clazz)) {
            throw new IllegalStateException("System does not exist: " + clazz.getName());
        }
        return clazz.cast(systems.get(clazz));
    }

    public List<EcsSystem> getSystems() {
        return systems.values().stream().toList();
    }

    public List<EcsSystem> getSystems(SystemType type) {
        return systems.values().stream().filter(s -> s.type() == type).toList();
    }
}
