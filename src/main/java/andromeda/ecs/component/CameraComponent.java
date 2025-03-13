package andromeda.ecs.component;

import andromeda.projection.PerspectiveCamera;
import andromeda.window.Screen;
import org.joml.Vector3f;

public class CameraComponent implements Component {

    private PerspectiveCamera perspectiveCamera;
    public Vector3f targetPosition, targetCameraForward;
    public float pitch = -5.0f, yaw = 0.0f;
    private boolean isMainCamera = false;

    public CameraComponent() {
        this.perspectiveCamera = new PerspectiveCamera(Screen.width, Screen.height);
        this.targetPosition = new Vector3f(-11.0f, 3.0f, 0.0f);
        this.targetCameraForward = new Vector3f(0, 0.0f, -1.0f);
    }

    public PerspectiveCamera getProjectionCamera() {
        return perspectiveCamera;
    }

    public void setProjectionCamera(PerspectiveCamera perspectiveCamera) {
        this.perspectiveCamera = perspectiveCamera;
    }

    public boolean isMainCamera() {
        return isMainCamera;
    }

    public void setMainCamera(boolean mainCamera) {
        isMainCamera = mainCamera;
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.CAMERA;
    }

    @Override
    public Component createComponent() {
        return new CameraComponent();
    }
}
