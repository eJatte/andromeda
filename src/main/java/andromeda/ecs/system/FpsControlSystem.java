package andromeda.ecs.system;

import andromeda.Controller;
import andromeda.DeltaTime;
import andromeda.ecs.Ecs;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.component.FpsControl;
import andromeda.ecs.component.Transform;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Set;

import static andromeda.util.GraphicsMath.DEG2RAD;

public class FpsControlSystem extends EcsSystem {
    public FpsControlSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.FPS_CONTROL, ComponentType.TRANSFORM));
    }

    @Override
    public void onAdd(int entityId) {
        Transform transform = ecs.getComponent(Transform.class, entityId);
        FpsControl fpsControl = ecs.getComponent(FpsControl.class, entityId);


        fpsControl.targetPosition = transform.getPosition();

        fpsControl.targetPitch = transform.getEulerRotation().x;
        fpsControl.targetYaw = transform.getEulerRotation().y;
    }

    @Override
    public void update() {
        for (var entity : this.getEntities(Signature.of(ComponentType.FPS_CONTROL, ComponentType.TRANSFORM))) {
            updateCamera(entity);
        }
    }

    private void updateCamera(int entityId) {
        Transform transform = ecs.getComponent(Transform.class, entityId);
        FpsControl fpsControl = ecs.getComponent(FpsControl.class, entityId);

        updatePosition(transform, fpsControl);
        calculateAngles(transform, fpsControl);
    }

    private void updatePosition(Transform transform, FpsControl fpsControl) {
        var forward = getCameraForward(transform);
        float speed = 20f * 2.0f * fpsControl.movementSpeed * DeltaTime.deltaTime;

        if (Input.get().key(KeyCode.KEY_LEFT_SHIFT)) {
            speed *= 3;
        } else if (Input.get().key(KeyCode.KEY_LEFT_CONTROL)) {
            speed /= 3;
        }

        var direction = new Vector3f(0);
        var cameraRight = forward.cross(new Vector3f(0, 1, 0), new Vector3f());

        if (Input.get().key(KeyCode.KEY_W))
            direction.add(forward);
        if (Input.get().key(KeyCode.KEY_S))
            direction.sub(forward);

        if (Input.get().key(KeyCode.KEY_D))
            direction.add(cameraRight);
        if (Input.get().key(KeyCode.KEY_A))
            direction.sub(cameraRight);

        if (Input.get().key(KeyCode.KEY_E))
            direction.add(new Vector3f(0, 1, 0));
        if (Input.get().key(KeyCode.KEY_Q))
            direction.sub(new Vector3f(0, 1, 0));

        if (direction.length() > 0)
            direction.normalize().mul(speed);

        if (Controller.MOUSE_ENABLED) {
            direction = new Vector3f(0);
        }

        fpsControl.targetPosition.add(direction);

        var diffPos = fpsControl.targetPosition.sub(transform.getPosition(), new Vector3f());
        transform.translate(diffPos.mul(fpsControl.movementSmoothing));
    }

    // TODO remove this create refactor the camera component so that it can be used more directly
    private Vector3f getCameraForward(Transform transform) {

        Vector3f forward = new Vector3f(0, 0.0f, -1.0f);
        new Vector4f(forward, 0).mul(transform.getLocalTransform(), new Vector4f()).xyz(forward).normalize();

        return forward;
    }

    private void calculateAngles(Transform transform, FpsControl fpsControl) {
        var mouseDelta = Input.get().isMouseEnabled() ? Input.get().getMouseDelta() : new Vector2f(0);

        float speed = 140f * 2 * fpsControl.rotationSpeed * DeltaTime.deltaTime;

        var yawDelta = -mouseDelta.x / 720.0f;
        float yaw = yawDelta * 40.0f * speed;

        var pitchDelta = -mouseDelta.y / 720.0f;
        float pitch = pitchDelta * 40.0f * speed;

        fpsControl.targetYaw += yaw;
        fpsControl.targetPitch += pitch;

        fpsControl.targetPitch = Math.max(-89.0f, Math.min(89.0f, fpsControl.targetPitch));

        Quaternionf targetRotation = new Quaternionf()
                .rotateY(DEG2RAD * fpsControl.targetYaw)
                .rotateX(DEG2RAD * fpsControl.targetPitch);

        transform.rotateSlerp(targetRotation, fpsControl.rotationSmoothing);
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
