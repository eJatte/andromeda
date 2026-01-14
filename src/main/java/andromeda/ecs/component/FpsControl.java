package andromeda.ecs.component;

import org.joml.Vector3f;

public class FpsControl implements Component {

    public Vector3f targetPosition;
    public float targetYaw, targetPitch;
    public float movementSpeed = 0.5f, rotationSpeed = 0.5f, movementSmoothing = 0.2f, rotationSmoothing = 0.25f;

    public FpsControl() {
        this.targetPosition = new Vector3f();
        this.targetYaw = 0;
        this.targetPitch = 0;
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.FPS_CONTROL;
    }

    @Override
    public Component createComponent() {
        return new FpsControl();
    }
}
