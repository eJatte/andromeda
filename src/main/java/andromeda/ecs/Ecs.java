package andromeda.ecs;

import andromeda.ecs.component.*;
import andromeda.ecs.entity.EntityManager;
import andromeda.ecs.system.EcsSystem;
import andromeda.ecs.system.*;

import java.util.Collection;

public class Ecs {

    private final ComponentManager componentManager;
    private final EntityManager entityManager;
    private final SystemManager systemManager;

    public Ecs() {
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
        componentManager.registerComponent(new FpsControl());
        componentManager.registerComponent(new RigidBody());
        componentManager.registerComponent(new Perspective());
        componentManager.registerComponent(new DeathTimer());

        systemManager.registerSystem(new DebugSystem(this));

        systemManager.registerSystem(new TransformSystem(this));
        systemManager.registerSystem(new PropertiesSystem(this));
        systemManager.registerSystem(new CameraSystem(this));
        systemManager.registerSystem(new FpsControlSystem(this));
        systemManager.registerSystem(new EditorSystem(this));
        systemManager.registerSystem(new PhysicsSystem(this));
        systemManager.registerSystem(new DeathTimerSystem(this));
        systemManager.registerSystem(new RenderSystem(this));

        systemManager.getSystems().forEach(EcsSystem::init);
    }

    public void update() {
        systemManager.getSystems(SystemType.PHYSICS).forEach(EcsSystem::update);
        systemManager.getSystems(SystemType.LOOP).forEach(EcsSystem::update);
        systemManager.getSystems(SystemType.RENDER).forEach(EcsSystem::update);
    }

    public int createEntity() {
        int entity = entityManager.createEntity();
        this.addComponent(Transform.class, entity);
        return entity;
    }

    public void destroyEntity(int entityId) {
        systemManager.entityDestroyed(entityId);
        entityManager.destroyEntity(entityId);
    }

    public <T extends Component> T getComponent(Class<T> clazz, int entityId) {
        return componentManager.getComponent(clazz, entityId);
    }

    public <T extends Component> T addComponent(Class<T> clazz, int entityId) {
        var component = componentManager.addComponent(clazz, entityId);
        entityManager.getSignature(entityId).set(component.componentType());
        systemManager.entitySignatureUpdate(entityId, entityManager.getSignature(entityId));
        return component;
    }

    public Collection<Integer> getEntities() {
        return entityManager.getEntities();
    }

    public Signature getSignature(int entityId) {
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

    public Collection<Component> getComponents() {
        return componentManager.getComponents();
    }

    public void query(ComponentType... componentTypes ) {

    }
}
