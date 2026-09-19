package andromeda.ecs;

import andromeda.ecs.component.*;
import andromeda.ecs.entity.EntityManager;
import andromeda.ecs.event.CollisionEvent;
import andromeda.ecs.event.Event;
import andromeda.ecs.event.EventListener;
import andromeda.ecs.event.EventManager;
import andromeda.ecs.system.EcsSystem;
import andromeda.ecs.system.*;

import java.util.Collection;

public class Ecs {

    private final ComponentManager componentManager;
    private final EntityManager entityManager;
    private final SystemManager systemManager;
    private final EventManager eventManager;

    public Ecs() {
        componentManager = new ComponentManager();
        entityManager = new EntityManager();
        systemManager = new SystemManager();
        eventManager = new EventManager();
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
        componentManager.registerComponent(new SpotLightComponent());
        componentManager.registerComponent(new SphereCollider());

        systemManager.registerSystem(new CollisionSystem(this));
        systemManager.registerSystem(new PhysicsSystem(this));

        systemManager.registerSystem(new DebugSystem(this));

        systemManager.registerSystem(new TransformSystem(this));
        systemManager.registerSystem(new PropertiesSystem(this));
        systemManager.registerSystem(new CameraSystem(this));
        systemManager.registerSystem(new FpsControlSystem(this));

        systemManager.registerSystem(new RenderSystem(this));
        systemManager.registerSystem(new EditorSystem(this));
        systemManager.registerSystem(new DebugRenderSystem(this));

        eventManager.registerEvent(new CollisionEvent());

        systemManager.getSystems().forEach(EcsSystem::init);
    }

    public void update() {
        systemManager.getSystems(SystemType.PHYSICS).forEach(EcsSystem::update);
        eventManager.triggerEvents();
        systemManager.getSystems(SystemType.LOOP).forEach(EcsSystem::update);
        eventManager.triggerEvents();
        systemManager.getSystems(SystemType.RENDER).forEach(EcsSystem::update);
        systemManager.getSystems(SystemType.DEBUG_RENDER).forEach(EcsSystem::update);
        systemManager.getSystems(SystemType.CLEANUP).forEach(EcsSystem::update);
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

    public <T extends Component> T addComponent(T component, int entityId) {
        var c = componentManager.addComponent(component, entityId);
        entityManager.getSignature(entityId).set(c.componentType());
        systemManager.entitySignatureUpdate(entityId, entityManager.getSignature(entityId));
        return c;
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

    public <T extends Event> void raiseEvent(Class<T> clazz, T event) {
        eventManager.raiseEvent(clazz, event);
    }

    public <T extends Event> void registerListener(Class<T> clazz, EventListener<T> eventListener) {
        eventManager.registerListener(clazz, eventListener);
    }

    public void query(ComponentType... componentTypes ) {

    }
}
