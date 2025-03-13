package andromeda.ecs.system;

import andromeda.DeltaTime;
import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.*;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.material.Material;
import andromeda.material.Texture;
import andromeda.window.Screen;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class EditorSystem extends EcsSystem {

    private RenderSystem renderSystem;
    private CameraSystem cameraSystem;
    private TransformSystem transformSystem;

    public EditorSystem(EcsCoordinator ecsCoordinator) {
        super(List.of(ComponentType.TRANSFORM), ecsCoordinator);
    }

    @Override
    public void init() {
        this.renderSystem = this.ecsCoordinator.getSystem(RenderSystem.class);
        this.cameraSystem = this.ecsCoordinator.getSystem(CameraSystem.class);
        this.transformSystem = this.ecsCoordinator.getSystem(TransformSystem.class);
    }

    @Override
    public void update() {
        if (!this.ecsCoordinator.getSystem(PropertiesSystem.class).isPlayMode()) {
            ImGui.dockSpaceOverViewport();
            ImGui.showDemoWindow();
            entitiesTab();
            viewportTab();
            performanceTab();
        } else {
            Screen.VIEWPORT_WIDTH = Screen.width;
            Screen.VIEWPORT_HEIGHT = Screen.height;
        }
    }

    private int operation = -1;

    private void viewportTab() {
        int renderTextureId = renderSystem.getRenderTextureId();
        var camera = this.cameraSystem.getCurrentMainCamera();

        ImGuizmo.enable(true);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, new ImVec2(0, 0));
        ImGui.begin("Viewport", null);

        var viewportSize = ImGui.getContentRegionAvail();
        Screen.VIEWPORT_HEIGHT = (int) viewportSize.y;
        Screen.VIEWPORT_WIDTH = (int) viewportSize.x;
        var rectPos = ImGui.getCursorScreenPos();
        ImGui.image(renderTextureId, viewportSize, new ImVec2(0, 1), new ImVec2(1, 0));


        ImGuizmo.setOrthographic(false);

        ImGuizmo.setDrawList();

        activeOperation();

        if (operation != -1 && currentEntity != -1) {
            ImGuizmo.setRect(rectPos.x, rectPos.y, viewportSize.x, viewportSize.y);
            var view = camera.getView().get(new float[16]);
            var proj = camera.getProjectionWH(Screen.VIEWPORT_WIDTH, Screen.VIEWPORT_HEIGHT).get(new float[16]);

            var transform = ecsCoordinator.getComponent(Transform.class, currentEntity);
            Matrix4f parentGlobalTransform = transform.parentEntityId != -1 ? transformSystem.getGlobalTransform(transform.parentEntityId) : new Matrix4f();
            Matrix4f entityLocalTransform = transform.localTransform;
            Matrix4f entityGlobalTransform = parentGlobalTransform.mul(entityLocalTransform, new Matrix4f());
            float[] matrix = entityGlobalTransform.get(new float[16]);

            ImGuizmo.manipulate(view, proj, operation, Mode.WORLD, matrix);
            Matrix4f transformed = new Matrix4f().set(matrix);
            transform.localTransform = parentGlobalTransform.invert(new Matrix4f()).mul(transformed);
        }

        ImGui.end();
        ImGui.popStyleVar();
    }

    private void activeOperation() {
        int newOperation = -1;
        if (Input.get().keyUp(KeyCode.KEY_T)) {
            newOperation = Operation.TRANSLATE;
        }
        if (Input.get().keyUp(KeyCode.KEY_Y)) {
            newOperation = Operation.SCALE;
        }
        if (Input.get().keyUp(KeyCode.KEY_U)) {
            newOperation = Operation.ROTATE;
        }

        if (newOperation == operation && operation != -1) {
            operation = -1;
        } else if (newOperation != -1) {
            operation = newOperation;
        }
    }

    float[] frame_times = new float[60 * 3];
    int offset = 0;

    private void performanceTab() {
        ImGui.begin("Performance");
        frame_times[offset] = DeltaTime.deltaTime * 1000.0f;
        ImGui.text("Frame Time");
        ImGui.plotLines("##Frame Time", frame_times, frame_times.length, offset, "ms", 0, 32, new ImVec2(0, 80));
        ImGui.text("FPS: %s".formatted(1000.0f / averageFps(frame_times)));
        offset = (offset + 1) % frame_times.length;
        ImGui.end();
    }

    private float averageFps(float[] frame_times) {
        float avg = 0;
        for (var f : frame_times) {
            avg += f;
        }
        return avg / frame_times.length;
    }

    int currentEntity = 0;
    int selectedEntityId = -1;

    private void entitiesTab() {
        ImGui.begin("Scene Hierarchy");
        handleEntitiesTree();
        ImGui.end();
        currentEntity = selectedEntityId;

        ImGui.begin("Entity");
        if (currentEntity != -1) {
            var transform = ecsCoordinator.getComponent(Transform.class, currentEntity);
            if (transform != null && ImGui.collapsingHeader("Transform")) {
                handleTransformComponent(currentEntity);
            }

            var model = ecsCoordinator.getComponent(EcsModel.class, currentEntity);
            if (model != null && ImGui.collapsingHeader("Model")) {
                handleEcsModelComponent(model);
            }

            var pointlight = ecsCoordinator.getComponent(PointLightComponent.class, currentEntity);
            if (pointlight != null && ImGui.collapsingHeader("Point Light")) {
                handlePointLightComponent(pointlight);
            }

            var directionalLight = ecsCoordinator.getComponent(DirectionalLightComponent.class, currentEntity);
            if (directionalLight != null && ImGui.collapsingHeader("Direction Light")) {
                handleDirectionalLightComponent(directionalLight);
            }
        }

        ImGui.end();
    }

    private void handleEntitiesTree() {
        List<TransformSystem.Node> transformHierarchy = transformSystem.getTransformHierarchy();

        for (var node : transformHierarchy) {
            handleNode(node);
        }
    }

    private void handleNode(TransformSystem.Node node) {
        int flags = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
        boolean hasChildren = !node.children.isEmpty();
        if (!hasChildren) {
            flags |= ImGuiTreeNodeFlags.Leaf;
        }

        if (node.entityId == selectedEntityId) {
            flags |= ImGuiTreeNodeFlags.Selected;
        }

        boolean open = ImGui.treeNodeEx(Integer.toString(node.entityId), flags, "entity_" + node.entityId);

        if (ImGui.isItemClicked()) {
            selectedEntityId = selectedEntityId == node.entityId ? -1 : node.entityId;
        }

        if (open) {
            if (hasChildren) {
                node.children.forEach(this::handleNode);
            }
            ImGui.treePop();
        }
    }

    private void handleTransformComponent(int entityId) {
        transformSystem.setLocalPosition(new Vector3f(pickVector3f("position", transformSystem.getLocalPosition(entityId))), entityId);
        transformSystem.setScale(new Vector3f(pickVector3f("scale", transformSystem.getScale(entityId))), entityId);
        transformSystem.setEulerRotation(new Vector3f(pickVector3f("rotation", transformSystem.getEulerRotation(entityId))), entityId);
    }

    private void handleEcsModelComponent(EcsModel model) {
        int i = 0;
        for (var mesh : model.getMeshes()) {
            if (ImGui.collapsingHeader("Mesh" + i)) {
                var material = mesh.getMaterial();
                handleMaterial(material);
            }
            i++;
        }
    }

    private void handlePointLightComponent(PointLightComponent pointLight) {
        pointLight.color.set(pickColor("color", pointLight.color));
        pointLight.radius = pickFloat("radius", pointLight.radius, 0.05f);
    }

    private void handleDirectionalLightComponent(DirectionalLightComponent directionalLight) {
        directionalLight.color.set(pickColor("color", directionalLight.color));
        directionalLight.direction.set(pickVector3f("direction", directionalLight.direction));
        directionalLight.castShadows = pickBoolean("cast shadows", directionalLight.castShadows);
    }

    private void handleMaterial(Material material) {
        material.diffuse.set(pickColor("diffuse", material.diffuse));
        material.specular.set(pickColor("specular", material.specular));
        material.ambient.set(pickColor("ambient", material.ambient));

        material.shininess = pickFloat("shininess", material.shininess);

        material.unlit = pickBoolean("unlit", material.unlit);
        ImGui.sameLine();
        material.wireFrame = pickBoolean("wireframe", material.wireFrame);

        material.texture_scale.set(pickVector2f("texture scale", material.texture_scale));


        if (material.diffuse_texture != null)
            displayTexture("diffuse texture", material.diffuse_texture);
        if (material.normal_texture != null)
            displayTexture("normal texture", material.normal_texture);
        if (material.roughness_texture != null)
            displayTexture("roughness texture", material.roughness_texture);
    }

    private void displayTexture(String name, Texture texture) {
        ImGui.text(name);
        var uv0 = new ImVec2(0, 0);
        var uv1 = new ImVec2(1, 1);
        ImGui.image(texture.getTexture_id(), new ImVec2(50, 50), uv0, uv1);
    }

    private float[] pickColor(String name, Vector3f original) {
        float[] color = new float[]{original.x, original.y, original.z};
        ImGui.colorEdit3(name, color);
        return color;
    }

    private float pickFloat(String name, float original) {
        return pickFloat(name, original, 1);
    }

    private float pickFloat(String name, float original, float speed) {
        float[] f = new float[]{original};
        ImGui.dragFloat(name, f, speed);
        return f[0];
    }

    private boolean pickBoolean(String name, boolean original) {
        ImBoolean imBoolean = new ImBoolean(original);
        ImGui.checkbox(name, imBoolean);
        return imBoolean.get();
    }

    private float[] pickVector2f(String name, Vector2f original) {
        float[] v2 = new float[]{original.x, original.y};
        ImGui.dragFloat2(name, v2, 0.05f);
        return v2;
    }

    private float[] pickVector3f(String name, Vector3f original) {
        float[] v3 = new float[]{original.x, original.y, original.z};
        ImGui.dragFloat3(name, v3, 0.05f);
        return v3;
    }

    @Override
    public SystemType type() {
        return SystemType.RENDER;
    }
}
