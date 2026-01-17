package andromeda.ecs.component;

import andromeda.ecs.entity.EntityManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentManager {

    // implement a "clickable" component and a "clickable" system????
    // The system can render to entities to a "texture" the size of the screen, the texture could contain the id of the entity!!!!!

    private Map<Class<?>, Component[]> entityComponentMap;
    private Map<Class<?>, Component> componentMap;

    public ComponentManager() {
        entityComponentMap = new HashMap<>();
        componentMap = new HashMap<>();
    }

    public <T extends Component> void registerComponent(T component) {
        var components = new Component[EntityManager.MAX_ENTITIES];
        if (entityComponentMap.containsKey(component.getClass())) {
            throw new IllegalStateException("Component type already registered");
        }
        entityComponentMap.put(component.getClass(), components);
        componentMap.put(component.getClass(), component);
    }

    public <T extends Component> T addComponent(Class<T> clazz, int entityId) {
        if (!componentMap.containsKey(clazz)) {
            throw new IllegalStateException("Component type not registered " + clazz.getName());
        }

        var componentBlueprint = componentMap.get(clazz);
        var component = componentBlueprint.createComponent();

        entityComponentMap.get(clazz)[entityId] = component;

        return clazz.cast(component);
    }

    public <T extends Component> T addComponent(T component, int entityId) {
        var clazz = component.getClass();

        if (!componentMap.containsKey(clazz)) {
            throw new IllegalStateException("Component type not registered " + clazz.getName());
        }

        entityComponentMap.get(clazz)[entityId] = component;

        return component;
    }

    public <T extends Component> T getComponent(Class<T> clazz, int entityId) {
        if (!componentMap.containsKey(clazz)) {
            throw new IllegalStateException("Component type not registered " + clazz.getName());
        }

        return clazz.cast(entityComponentMap.get(clazz)[entityId]);
    }

    public Collection<Component> getComponents() {
        return componentMap.values();
    }

}
