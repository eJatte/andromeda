package andromeda.ecs.system;

import andromeda.DeltaTime;
import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.CameraComponent;
import andromeda.ecs.component.ComponentType;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.projection.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

import static org.joml.Math.*;

public class CameraSystem extends EcsSystem {
    private int currentMainCamera = -1;

    public CameraSystem(EcsCoordinator ecsCoordinator) {
        super(List.of(ComponentType.TRANSFORM, ComponentType.CAMERA), ecsCoordinator);
    }

    @Override
    public void update() {
        for (var entity : this.entities) {
            var cameraComponent = ecsCoordinator.getComponent(CameraComponent.class, entity);
            if (cameraComponent.isMainCamera()) {
                if (currentMainCamera != entity) {
                    currentMainCamera = entity;
                    newMainCamera(cameraComponent);
                }
                updateCamera(cameraComponent);
            }
        }
    }

    public Camera getCurrentMainCamera() {
        return ecsCoordinator.getComponent(CameraComponent.class, this.currentMainCamera).getProjectionCamera();
    }

    public int getCurrentMainCameraEntity() {
        return this.currentMainCamera;
    }

    public void setMainCamera(int entityId) {
        if( entityId != this.currentMainCamera) {
            var mainCameraComponent = ecsCoordinator.getComponent(CameraComponent.class, this.currentMainCamera);
            var newMainCameraComponent = ecsCoordinator.getComponent(CameraComponent.class, entityId);

            mainCameraComponent.setMainCamera(false);
            newMainCameraComponent.setMainCamera(true);

            this.currentMainCamera = entityId;
            newMainCamera(newMainCameraComponent);
        }
    }

    private void newMainCamera(CameraComponent cameraComponent) {
        cameraComponent.getProjectionCamera().setPosition(cameraComponent.targetPosition);
        cameraComponent.targetCameraForward = calculateCameraForward(cameraComponent);
        cameraComponent.getProjectionCamera().setCameraForward(cameraComponent.targetCameraForward);

        var camera = cameraComponent.getProjectionCamera();
        var cameraRight = camera.getCameraForward().cross(new Vector3f(0, 1, 0), new Vector3f());
        camera.setCameraUp(cameraRight.cross(camera.getCameraForward(), new Vector3f()));
    }

    private void updateCamera(CameraComponent cameraComponent) {
        var camera = cameraComponent.getProjectionCamera();

        float speed = 10f * DeltaTime.deltaTime;

        if (Input.get().key(KeyCode.KEY_LEFT_SHIFT)) {
            speed *= 3;
        }

        var cameraRight = camera.getCameraForward().cross(new Vector3f(0, 1, 0), new Vector3f());
        camera.setCameraUp(cameraRight.cross(camera.getCameraForward(), new Vector3f()));

        var direction = new Vector3f(0);

        if (Input.get().key(KeyCode.KEY_W))
            direction.add(camera.getCameraForward());
        if (Input.get().key(KeyCode.KEY_S))
            direction.sub(camera.getCameraForward());

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


        cameraComponent.targetPosition.add(direction);
        var diffPos = cameraComponent.targetPosition.sub(camera.getPosition(), new Vector3f());
        camera.getPosition().add(diffPos.mul(0.2f));

        cameraComponent.targetCameraForward = calculateCameraForward(cameraComponent);

        var diffFor = cameraComponent.targetCameraForward.sub(camera.getCameraForward(), new Vector3f());
        camera.getCameraForward().add(diffFor.mul(0.2f));
        camera.getCameraForward().normalize();
    }

    private Vector3f calculateCameraForward(CameraComponent cameraComponent) {
        var mouseDelta = Input.get().isMouseEnabled() ? Input.get().getMouseDelta() : new Vector2f(0);

        float speed = 140f * DeltaTime.deltaTime;

        var pitchDelta = -mouseDelta.y / 720.0f;
        cameraComponent.pitch += pitchDelta * 40.0f * speed;
        if (cameraComponent.pitch > 89.0f)
            cameraComponent.pitch = 89.0f;
        if (cameraComponent.pitch < -89.0f)
            cameraComponent.pitch = -89.0f;

        var yawDelta = mouseDelta.x / 720.0f;
        cameraComponent.yaw += yawDelta * 40.0f * speed;

        var front = new Vector3f(0);
        front.x = cos(toRadians(cameraComponent.yaw)) * cos(toRadians(cameraComponent.pitch));
        front.y = sin(toRadians(cameraComponent.pitch));
        front.z = sin(toRadians(cameraComponent.yaw)) * cos(toRadians(cameraComponent.pitch));
        front.normalize();

        return front;
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
