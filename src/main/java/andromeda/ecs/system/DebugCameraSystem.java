package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.CameraComponent;
import andromeda.ecs.component.EcsModel;
import andromeda.ecs.component.Transform;
import andromeda.geometry.Mesh;
import andromeda.geometry.Primitives;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.material.Material;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class DebugCameraSystem extends EcsSystem {

    private CameraSystem cameraSystem;
    private int debugProjectionFrustumEntity;
    private int debugCamera, mainCamera;
    private boolean inDebug = false;

    public DebugCameraSystem(EcsCoordinator ecsCoordinator) {
        super(List.of(), ecsCoordinator);
    }

    @Override
    public void update() {
        if (Input.get().keyUp(KeyCode.KEY_T)) {
            if (!inDebug) {
                calculateFrustum();
                enableDebugFrustum();
                inDebug = true;
                enableDebugCamera();
            } else {
                disableDebugCamera();
                disableDebugFrustum();
                inDebug = false;
            }

        }
    }

    @Override
    public void init() {
        cameraSystem = ecsCoordinator.getSystem(CameraSystem.class);

        debugCamera = ecsCoordinator.createEntity();
        CameraComponent cameraComponent = ecsCoordinator.addComponent(CameraComponent.class, debugCamera);

        initDebug();
    }

    private void initDebug() {
        debugProjectionFrustumEntity = ecsCoordinator.createEntity();
        var modelComponent = ecsCoordinator.addComponent(EcsModel.class, debugProjectionFrustumEntity);
        var geometry = Primitives.ndcCube();
        geometry.upload();
        var material = new Material();
        material.diffuse = new Vector3f(1, 1, 0);
        material.unlit = true;
        material.wireFrame = true;
        modelComponent.getMeshes().add(new Mesh(geometry, material));
    }

    private void calculateFrustum() {
        var transform = ecsCoordinator.getComponent(Transform.class, debugProjectionFrustumEntity);
        var proj = cameraSystem.getCurrentMainCamera().getProjection(5, 20);
        var view = cameraSystem.getCurrentMainCamera().getView();
        var pv = proj.mul(view, new Matrix4f());
        var inversePv = pv.invert(new Matrix4f());
        transform.localTransform = inversePv;
    }

    private void enableDebugFrustum() {
        var transform = ecsCoordinator.getComponent(Transform.class, debugProjectionFrustumEntity);
    }

    private void disableDebugFrustum() {
        var transform = ecsCoordinator.getComponent(Transform.class, debugProjectionFrustumEntity);
    }

    private void enableDebugCamera() {
        mainCamera = cameraSystem.getCurrentMainCameraEntity();
        var mainCameraComp = ecsCoordinator.getComponent(CameraComponent.class, mainCamera);
        var debugCameraComp = ecsCoordinator.getComponent(CameraComponent.class, debugCamera);

        debugCameraComp.targetCameraForward = new Vector3f(mainCameraComp.targetCameraForward);
        debugCameraComp.targetPosition = new Vector3f(mainCameraComp.targetPosition);
        debugCameraComp.pitch = mainCameraComp.pitch;
        debugCameraComp.yaw = mainCameraComp.yaw;

        cameraSystem.setMainCamera(debugCamera);
    }

    private void disableDebugCamera() {
        cameraSystem.setMainCamera(mainCamera);
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
