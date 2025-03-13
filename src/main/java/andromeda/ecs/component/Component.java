package andromeda.ecs.component;

public interface Component {

    ComponentType componentType();

    Component createComponent();
}
