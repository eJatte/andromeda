package andromeda.ecs.system;

import andromeda.Controller;
import andromeda.DeltaTime;
import andromeda.config.AmbientOcclusionConfig;
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
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EditorSystem extends EcsSystem {
    private CameraSystem cameraSystem;
    private TransformSystem transformSystem;
    private RenderSystem renderSystem;

    public EditorSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public void init() {
        this.cameraSystem = this.ecs.getSystem(CameraSystem.class);
        this.transformSystem = this.ecs.getSystem(TransformSystem.class);
        this.renderSystem = this.ecs.getSystem(RenderSystem.class);
    }

    public void setSelectedEntityId(int entityId) {
        selectedEntityId = selectedEntityId == entityId ? -1 : entityId;
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.TRANSFORM));
    }

    @Override
    public void update() {


        if (!Controller.MOUSE_ENABLED)
            ImGui.beginDisabled();

        ImGui.dockSpaceOverViewport(ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode);

        //ImGui.showDemoWindow();

        renderGizmos();

        if (!this.ecs.getSystem(PropertiesSystem.class).hideGUI()) {
            performanceTab();
            graphicsTab();
            entitiesTab();
        }

        if (!Controller.MOUSE_ENABLED)
            ImGui.endDisabled();

        if (Controller.MOUSE_ENABLED) {
            if (Input.get().keyDown(KeyCode.MOUSE_BUTTON_LEFT) && !isUiInUse()) {
                Vector2f mPosf = Input.get().getMousePosition();
                Vector2i mPos = new Vector2i((int) mPosf.x, (int) mPosf.y);
                this.setSelectedEntityId(this.renderSystem.readEntityId(mPos));
            }
            if (Input.get().keyUp(KeyCode.KEY_D) && Input.get().key(KeyCode.KEY_LEFT_CONTROL) && selectedEntityId != -1) {
                selectedEntityId = duplicateEntity(selectedEntityId, -1);
            }
        }
    }

    private int duplicateEntity(int entityId, int parentId) {
        int newEntity = ecs.createEntity();

        for (Component c : ecs.getComponents()) {
            var component = ecs.getComponent(c.getClass(), entityId);
            if (component != null) {
                var newComponent = component.copy();
                ecs.addComponent(newComponent, newEntity);
            }
        }
        Transform transform = ecs.getComponent(Transform.class, newEntity);
        if(!transform.getName().endsWith("(copy)"))
            transform.setName(transform.getName() + " (copy)");

        transformSystem.getChildren(entityId).forEach(child -> duplicateEntity(child, newEntity));
        if(parentId != -1) {
            transformSystem.setParent(newEntity, parentId);
        }
        else {
            transform.setParentEntityId(-1);
            transform.setLocalTransform(transformSystem.getGlobalTransform(entityId));
        }
        return newEntity;
    }

    private boolean isUiInUse() {
        boolean imGuiHovered = ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow | ImGuiHoveredFlags.AllowWhenBlockedByActiveItem);
        boolean gizmoInUse = ImGuizmo.isUsing() || ImGuizmo.isUsing();
        return imGuiHovered || gizmoInUse;
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);
        if (selectedEntityId == entityId) {
            selectedEntityId = -1;
        }
    }

    private int operation = -1;

    private void renderGizmos() {
        if (Controller.MOUSE_ENABLED) {
            var camera = this.cameraSystem.getCurrentMainCamera();
            ImGuizmo.enable(true);
            ImGuizmo.setOrthographic(false);

            ImGuizmo.setDrawList(ImGui.getBackgroundDrawList());

            activeOperation();

            if (operation != -1 && selectedEntityId != -1) {
                ImGuizmo.setRect(0, 0, Screen.width, Screen.height);
                var view = camera.getView().get(new float[16]);
                var proj = camera.getProjectionWH(Screen.width, Screen.height).get(new float[16]);

                var transform = ecs.getComponent(Transform.class, selectedEntityId);
                Matrix4f parentGlobalTransform = transform.getParentEntityId() != -1 ? transformSystem.getGlobalTransform(transform.getParentEntityId()) : new Matrix4f();
                Matrix4f entityLocalTransform = transform.getLocalTransform();
                Matrix4f entityGlobalTransform = parentGlobalTransform.mul(entityLocalTransform, new Matrix4f());
                float[] matrix = entityGlobalTransform.get(new float[16]);

                ImGuizmo.manipulate(view, proj, operation, Mode.WORLD, matrix);
                Matrix4f transformed = new Matrix4f().set(matrix);
                transform.setLocalTransform(parentGlobalTransform.invert(new Matrix4f()).mul(transformed));
            }
        }
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
        ImGui.begin("Performance", ImGuiWindowFlags.NoBackground);
        frame_times[offset] = DeltaTime.deltaTime * 1000.0f;
        ImGui.text("Frame Time");
        ImGui.plotLines("##Frame Time", frame_times, frame_times.length, offset, "ms", 0, 32, new ImVec2(0, 80));
        ImGui.text("FPS: %s".formatted(1000.0f / averageFps(frame_times)));
        offset = (offset + 1) % frame_times.length;
        ImGui.end();
    }

    private void graphicsTab() {
        ImGui.begin("Graphics Settings", ImGuiWindowFlags.NoBackground);
        ImGui.text("Ambient Occlusion");
        AmbientOcclusionConfig.radius = pickFloatSlider("radius", AmbientOcclusionConfig.radius, 0, 3);
        AmbientOcclusionConfig.power = pickFloatSlider("power", AmbientOcclusionConfig.power, 0.1f, 3);
        AmbientOcclusionConfig.bias = pickFloatSlider("bias", AmbientOcclusionConfig.bias, 0f, 0.1f);
        AmbientOcclusionConfig.n_samples = pickInt("n_samples", AmbientOcclusionConfig.n_samples, 1, 1, 128);

        ImGui.end();
    }

    private float averageFps(float[] frame_times) {
        float avg = 0;
        for (var f : frame_times) {
            avg += f;
        }
        return avg / frame_times.length;
    }

    int selectedEntityId = -1;

    private void entitiesTab() {
        ImGui.begin("Scene Hierarchy", ImGuiWindowFlags.NoBackground);

        ImGui.separatorText("Entities");
        ImGui.beginChild("childEntities", new ImVec2(ImGui.getContentRegionAvail().x, ImGui.getContentRegionAvail().y * 0.8f));
        handleEntitiesTree();
        ImGui.endChild();

        ImGui.separator();
        if (ImGui.button("Create Entity")) {
            ecs.createEntity();
        }
        ImGui.sameLine();
        if (ImGui.button("Delete Entity")) {
            ecs.destroyEntity(selectedEntityId);
        }
        ImGui.end();


        ImGui.begin("Entity", ImGuiWindowFlags.NoBackground);
        if (selectedEntityId != -1) {
            var transform = ecs.getComponent(Transform.class, selectedEntityId);
            if (transform != null && ImGui.collapsingHeader("Transform")) {
                handleTransformComponent(selectedEntityId);
            }

            var model = ecs.getComponent(EcsModel.class, selectedEntityId);
            if (model != null && ImGui.collapsingHeader("Model")) {
                handleEcsModelComponent(model);
            }

            var pointlight = ecs.getComponent(PointLightComponent.class, selectedEntityId);
            if (pointlight != null && ImGui.collapsingHeader("Point Light")) {
                handlePointLightComponent(pointlight);
            }

            var directionalLight = ecs.getComponent(DirectionalLightComponent.class, selectedEntityId);
            if (directionalLight != null && ImGui.collapsingHeader("Direction Light")) {
                handleDirectionalLightComponent(directionalLight);
            }

            var fpsCameraComponent = ecs.getComponent(FpsControl.class, selectedEntityId);
            if (fpsCameraComponent != null && ImGui.collapsingHeader("FPS Camera")) {
                handleFpsCameraComponent(fpsCameraComponent);
            }

            handleAddComponent(selectedEntityId);
        }

        ImGui.end();
    }

    private void handleAddComponent(int entityId) {
        Collection<Component> components = ecs.getComponents();

        if (ImGui.button("Add Component")) {
            ImGui.openPopup("add_component_popup");
        }

        if (ImGui.beginPopup("add_component_popup")) {

            ImGui.separatorText("Components");
            for (Component component : components) {
                if (ImGui.selectable(component.getClass().getSimpleName())) {
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

        } else if (nodeHasSelectedChild(node)) {
            ImGui.setNextItemOpen(true);
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

    private boolean nodeHasSelectedChild(TransformSystem.Node node) {
        for (var n : node.children) {
            if (n.entityId == selectedEntityId) {
                return true;
            }
            if (nodeHasSelectedChild(n)) {
                return true;
            }
        }
        return false;
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

    private int pickInt(String name, int original) {
        return pickInt(name, original, 1);
    }

    private int pickInt(String name, int original, int speed) {
        int[] i = new int[]{original};
        ImGui.dragInt(name, i, speed);
        return i[0];
    }

    private int pickInt(String name, int original, int speed, int min, int max) {
        int[] i = new int[]{original};
        ImGui.dragInt(name, i, speed, min, max);
        return i[0];
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
