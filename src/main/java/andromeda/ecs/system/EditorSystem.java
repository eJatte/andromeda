package andromeda.ecs.system;

import andromeda.Controller;
import andromeda.DeltaTime;
import andromeda.ecs.Ecs;
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EditorSystem extends EcsSystem {

    private RenderSystem renderSystem;
    private CameraSystem cameraSystem;
    private TransformSystem transformSystem;

    public EditorSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public void init() {
        this.renderSystem = this.ecs.getSystem(RenderSystem.class);
        this.cameraSystem = this.ecs.getSystem(CameraSystem.class);
        this.transformSystem = this.ecs.getSystem(TransformSystem.class);
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.TRANSFORM));
    }

    @Override
    public void update() {
        if (!this.ecs.getSystem(PropertiesSystem.class).isPlayMode()) {
            ImGui.dockSpaceOverViewport();
            ImGui.showDemoWindow();

            viewportTab();

            if (!Controller.MOUSE_ENABLED)
                ImGui.beginDisabled();

            entitiesTab();
            performanceTab();

            if (!Controller.MOUSE_ENABLED)
                ImGui.endDisabled();
        } else {
            Screen.VIEWPORT_WIDTH = Screen.width;
            Screen.VIEWPORT_HEIGHT = Screen.height;
        }
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);
        if (selectedEntityId == entityId) {
            selectedEntityId = -1;
            currentEntity = -1;
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

        if (Controller.MOUSE_ENABLED) {
            ImGuizmo.setOrthographic(false);

            ImGuizmo.setDrawList();

            activeOperation();

            if (operation != -1 && currentEntity != -1) {
                ImGuizmo.setRect(rectPos.x, rectPos.y, viewportSize.x, viewportSize.y);
                var view = camera.getView().get(new float[16]);
                var proj = camera.getProjectionWH(Screen.VIEWPORT_WIDTH, Screen.VIEWPORT_HEIGHT).get(new float[16]);

                var transform = ecs.getComponent(Transform.class, currentEntity);
                Matrix4f parentGlobalTransform = transform.getParentEntityId() != -1 ? transformSystem.getGlobalTransform(transform.getParentEntityId()) : new Matrix4f();
                Matrix4f entityLocalTransform = transform.getLocalTransform();
                Matrix4f entityGlobalTransform = parentGlobalTransform.mul(entityLocalTransform, new Matrix4f());
                float[] matrix = entityGlobalTransform.get(new float[16]);

                ImGuizmo.manipulate(view, proj, operation, Mode.WORLD, matrix);
                Matrix4f transformed = new Matrix4f().set(matrix);
                transform.setLocalTransform(parentGlobalTransform.invert(new Matrix4f()).mul(transformed));
            }
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

        ImGui.separatorText("Entities");
        ImGui.beginChild("childEntities", new ImVec2(ImGui.getContentRegionAvail().x,ImGui.getContentRegionAvail().y * 0.8f ));
        handleEntitiesTree();
        ImGui.endChild();

        ImGui.separator();
        if(ImGui.button("Create Entity")) {
            ecs.createEntity();
        }
        ImGui.sameLine();
        if(ImGui.button("Delete Entity")) {
            ecs.destroyEntity(currentEntity);
        }
        ImGui.end();

        currentEntity = selectedEntityId;

        ImGui.begin("Entity");
        if (currentEntity != -1) {
            var transform = ecs.getComponent(Transform.class, currentEntity);
            if (transform != null && ImGui.collapsingHeader("Transform")) {
                handleTransformComponent(currentEntity);
            }

            var model = ecs.getComponent(EcsModel.class, currentEntity);
            if (model != null && ImGui.collapsingHeader("Model")) {
                handleEcsModelComponent(model);
            }

            var pointlight = ecs.getComponent(PointLightComponent.class, currentEntity);
            if (pointlight != null && ImGui.collapsingHeader("Point Light")) {
                handlePointLightComponent(pointlight);
            }

            var directionalLight = ecs.getComponent(DirectionalLightComponent.class, currentEntity);
            if (directionalLight != null && ImGui.collapsingHeader("Direction Light")) {
                handleDirectionalLightComponent(directionalLight);
            }

            var fpsCameraComponent = ecs.getComponent(FpsControl.class, currentEntity);
            if (fpsCameraComponent != null && ImGui.collapsingHeader("FPS Camera")) {
                handleFpsCameraComponent(fpsCameraComponent);
            }

            handleAddComponent(currentEntity);
        }

        ImGui.end();
    }

    private void handleAddComponent(int entityId) {
        Collection<Component> components = ecs.getComponents();

        if (ImGui.button("Add Component")) {
            ImGui.openPopup("add_component_popup");
        }

        if(ImGui.beginPopup("add_component_popup")) {

            ImGui.separatorText("Components");
            for(Component component : components) {
                if(ImGui.selectable(component.getClass().getSimpleName())){
                    ecs.addComponent(component.getClass(), entityId);
                }
            }

            ImGui.endPopup();
        }
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

        String name = ecs.getComponent(Transform.class, node.entityId).getName();

        name = name == null ? "entity_" + node.entityId : name;

        boolean open = ImGui.treeNodeEx(Integer.toString(node.entityId), flags, name);

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
        Transform transform = this.ecs.getComponent(Transform.class, entityId);

        Vector3f scale = new Vector3f(pickVector3f("scale", transform.getScale()));
        transform.setScale(scale);

        Vector3f position = new Vector3f(pickVector3f("position", transform.getPosition()));
        transform.setPosition(position);

        Vector3f curRotation = transform.getEulerRotation();
        Vector3f newRotation = new Vector3f(pickVector3f("rotation", curRotation));
        Vector3f diff = newRotation.sub(curRotation);
        if (diff.length() > 0)
            transform.rotateEuler(diff);
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
        pointLight.intensity = pickFloat("intensity", pointLight.intensity, 0.05f);
    }

    private void handleDirectionalLightComponent(DirectionalLightComponent directionalLight) {
        directionalLight.color.set(pickColor("color", directionalLight.color));
        directionalLight.castShadows = pickBoolean("cast shadows", directionalLight.castShadows);
        directionalLight.intensity = pickFloat("intensity", directionalLight.intensity, 0.05f);
    }

    private void handleFpsCameraComponent(FpsControl fpsControl) {
        fpsControl.movementSpeed = pickFloatSlider("M Speed", fpsControl.movementSpeed);
        fpsControl.movementSmoothing = pickFloatSlider("M Smoothing", fpsControl.movementSmoothing);
        fpsControl.rotationSpeed = pickFloatSlider("R Speed", fpsControl.rotationSpeed);
        fpsControl.rotationSmoothing = pickFloatSlider("R Smoothing", fpsControl.rotationSmoothing);
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

    private float pickFloatSlider(String name, float original) {
        return pickFloatSlider(name, original, 0, 1);
    }

    private float pickFloatSlider(String name, float original, float min, float max) {
        float[] f = new float[]{original};
        ImGui.sliderFloat(name, f, min, max);
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
