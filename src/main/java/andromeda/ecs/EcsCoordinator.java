package andromeda.ecs;

import andromeda.ecs.component.*;
import andromeda.ecs.entity.EntityManager;
import andromeda.ecs.system.EcsSystem;
import andromeda.ecs.system.*;

import java.util.BitSet;
import java.util.Collection;

public class EcsCoordinator {

    private final ComponentManager componentManager;
    private final EntityManager entityManager;
    private final SystemManager systemManager;

    public EcsCoordinator() {
        componentManager = new ComponentManager();
        entityManager = new EntityManager();
        systemManager = new SystemManager();
    }

    public void init() {
        componentManager.registerComponent(new Transform());
        componentManager.registerComponent(new EcsModel());
        componentManager.registerComponent(new DirectionalLightComponent());
        componentManager.registerComponent(new PointLightComponent());
        componentManager.registerComponent(new CameraComponent());

        systemManager.registerSystem(new DebugSystem(this));
        //systemManager.registerSystem(new RandomObjectsSystem(this));

        systemManager.registerSystem(new TransformSystem(this));
        systemManager.registerSystem(new PropertiesSystem(this));
        systemManager.registerSystem(new RenderSystem(this));
        //systemManager.registerSystem(new LightSystem(this));
        //systemManager.registerSystem(new DebugCameraSystem(this));
        systemManager.registerSystem(new CameraSystem(this));
        systemManager.registerSystem(new EditorSystem(this));

        systemManager.getSystems().forEach(EcsSystem::init);
    }

    public void update() {
        systemManager.getSystems(SystemType.LOOP).forEach(EcsSystem::update);
        systemManager.getSystems(SystemType.RENDER).forEach(EcsSystem::update);
    }

    public int createEntity() {
        int entity = entityManager.createEntity();
        this.addComponent(Transform.class, entity);
        return entity;
    }

    public <T extends Component> T getComponent(Class<T> clazz, int entityId) {
        return componentManager.getComponent(clazz, entityId);
    }

    public <T extends Component> T addComponent(Class<T> clazz, int entityId) {
        var component = componentManager.addComponent(clazz, entityId);
        entityManager.getSignature(entityId).set(component.componentType().id);
        systemManager.entitySignatureUpdate(entityId, entityManager.getSignature(entityId));
        return component;
    }

    public Collection<Integer> getEntities() {
        return entityManager.getEntities();
    }

    public BitSet getSignature(int entityId) {
        return entityManager.getSignature(entityId);
    }

    public <T extends EcsSystem> T getSystem(Class<T> clazz) {
        return systemManager.getSystem(clazz);
    }

    public void enableEntity(int entityId) {
        systemManager.entitySignatureUpdate(entityId, entityManager.getSignature(entityId));
    }

    public void disableEntity(int entityId) {
        systemManager.entityDestroyed(entityId);
    }
}
