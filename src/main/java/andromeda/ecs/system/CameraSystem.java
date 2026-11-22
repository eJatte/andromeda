package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.*;
import andromeda.geometry.Geometry;
import andromeda.geometry.Mesh;
import andromeda.geometry.Primitives;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.material.Material;
import andromeda.projection.Camera;
import andromeda.projection.EcsCamera;
import andromeda.util.Colors;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Set;

public class CameraSystem extends EcsSystem {
    private int mainCameraEntityId = -1;
    private TransformSystem transformSystem;
    private Geometry sphere;

    private Vector3f position, forward;

    public CameraSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public void init() {
        transformSystem = ecs.getSystem(TransformSystem.class);
        this.sphere = Primitives.cube();
        this.sphere.upload();
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.CAMERA, ComponentType.TRANSFORM, ComponentType.PERSPECTIVE));
    }

    @Override
    public void update() {
        mainCameraEntityId = getMainCameraEntityId();
        shoot(this.position, this.forward);
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

        // todo remove all of this, add a real "shoot item" or something
        this.position = position;
        this.forward = forward;

        return camera;
    }

    // todo remove all of this, add a real "shoot item" or something
    private void shoot(Vector3f position, Vector3f forward) {
        if (Input.get().keyUp(KeyCode.MOUSE_BUTTON_LEFT)) {
            int entityId = ecs.createEntity();
            Transform transform = ecs.getComponent(Transform.class, entityId);
            transform.setPosition(new Vector3f(forward.mul(3, new Vector3f()).add(position)));

            Material material = new Material();
            material.diffuse = Colors.Debug.RED;
            Mesh mesh = new Mesh(this.sphere, material);
            EcsModel ecsModel = ecs.addComponent(EcsModel.class, entityId);
            ecsModel.getMeshes().add(mesh);

            ecs.addComponent(SphereCollider.class, entityId);
            RigidBody rigidBody = ecs.addComponent(RigidBody.class, entityId);
            rigidBody.velocity.add(forward.mul(50, new Vector3f()));
        }
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
