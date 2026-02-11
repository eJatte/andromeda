package andromeda.ecs.component;

public class CameraComponent implements Component {
    public boolean mainCamera = false;

    public CameraComponent() {
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.CAMERA;
    }

    @Override
    public Component createComponent() {
        return new CameraComponent();
    }

    @Override
    public Component copy() {
        var comp = new CameraComponent();
        comp.mainCamera = this.mainCamera;
        return comp;
    }
}
