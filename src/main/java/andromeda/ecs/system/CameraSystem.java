package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.CameraComponent;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.component.Perspective;
import andromeda.ecs.component.Transform;
import andromeda.projection.Camera;
import andromeda.projection.EcsCamera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Set;

public class CameraSystem extends EcsSystem {
    private int mainCameraEntityId = -1;
    private TransformSystem transformSystem;

    public CameraSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public void init() {
        transformSystem = ecs.getSystem(TransformSystem.class);
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.CAMERA, ComponentType.TRANSFORM, ComponentType.PERSPECTIVE));
    }

    @Override
    public void update() {
        mainCameraEntityId = getMainCameraEntityId();
    }

    private int getMainCameraEntityId() {
        for (var entity : this.getEntities(Signature.of(ComponentType.CAMERA, ComponentType.TRANSFORM, ComponentType.PERSPECTIVE))) {
            CameraComponent cameraComponent = ecs.getComponent(CameraComponent.class, entity);
            if (cameraComponent.mainCamera)
                return entity;
        }
        return -1;
    }

    public Camera getCurrentMainCamera() {
        Transform transform = ecs.getComponent(Transform.class, this.mainCameraEntityId);
        Perspective perspective = ecs.getComponent(Perspective.class, this.mainCameraEntityId);

        Matrix4f matrix = transformSystem.getGlobalTransform(this.mainCameraEntityId);

        Vector3f forward = new Vector3f(0, 0.0f, -1.0f);
        new Vector4f(forward, 0).mul(matrix, new Vector4f()).xyz(forward).normalize();

        Vector3f up = new Vector3f(0, 1, 0);
        new Vector4f(up, 0).mul(matrix, new Vector4f()).xyz(up).normalize();

        Vector3f position = transformSystem.getGlobalPosition(this.mainCameraEntityId);

        var view = new Matrix4f().lookAt(position, position.add(forward, new Vector3f()), up);

        Camera camera = new EcsCamera(perspective, view);
        camera.setPosition(position);
        return camera;
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
