package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.component.Transform;
import imgui.extension.imguizmo.ImGuizmo;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformSystem extends EcsSystem {

    class Node {
        int entityId;
        Node parentNode;
        List<Node> children;

        public Node(int entityId) {
            this.entityId = entityId;
            this.children = new ArrayList<>();
            this.parentNode = null;
        }
    }

    private Map<Integer, Node> graph;

    public TransformSystem(EcsCoordinator ecsCoordinator) {
        super(List.of(ComponentType.TRANSFORM), ecsCoordinator);
        graph = new HashMap<>();
    }

    public List<Node> getTransformHierarchy() {
        return graph.values().stream().filter(n -> n.parentNode == null).toList();
    }

    public void setParent(int entityId, int parentId) {
        var transformComp = this.ecsCoordinator.getComponent(Transform.class, entityId);

        Node node = getNode(entityId);
        if (node.parentNode != null) {
            node.parentNode.children.remove(node);
        }

        Node parentNode = getNode(parentId);
        parentNode.children.add(node);
        node.parentNode = parentNode;
        transformComp.parentEntityId = parentId;
    }

    @Override
    public void addEntity(int entityId) {
        super.addEntity(entityId);

        Node node = getNode(entityId);
        var transformComp = this.ecsCoordinator.getComponent(Transform.class, entityId);
        if (transformComp.parentEntityId != -1) {
            Node parentNode = this.getNode(transformComp.parentEntityId);
            node.parentNode = parentNode;
            parentNode.children.add(node);
        }
    }

    private Node getNode(int entityId) {
        if (!graph.containsKey(entityId)) {
            graph.put(entityId, new Node(entityId));
        }
        return graph.get(entityId);
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);

    }

    @Override
    public void update() {

    }

    public Matrix4f getGlobalTransform(int entityId) {
        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        Matrix4f local = transform.localTransform;
        int parentEntity = transform.parentEntityId;
        if (parentEntity != -1) {
            return getGlobalTransform(parentEntity).mul(local, new Matrix4f());
        }
        return local;
    }

    public Vector3f getScale(int entityId) {
        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        return getScale(transform.localTransform);
    }

    private Vector3f getScale(Matrix4f transform) {
        float[] translation = new float[16];
        float[] rotation = new float[16];
        float[] scale = new float[16];

        ImGuizmo.decomposeMatrixToComponents(transform.get(new float[16]), translation, rotation, scale);

        return new Vector3f(scale[0], scale[1], scale[2]);
    }

    public void setScale(Vector3f scale_xyz, int entityId) {
        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        float[] translation = new float[16];
        float[] rotation = new float[16];
        float[] scale = new float[16];
        float[] matrix = transform.localTransform.get(new float[16]);

        ImGuizmo.decomposeMatrixToComponents(matrix, translation, rotation, scale);
        scale[0] = scale_xyz.x;
        scale[1] = scale_xyz.y;
        scale[2] = scale_xyz.z;
        ImGuizmo.recomposeMatrixFromComponents(translation, rotation, scale, matrix);

        transform.localTransform.set(matrix);
    }

    public Vector3f getEulerRotation(int entityId) {
        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        return getEulerRotation(transform.localTransform);
    }

    public void setEulerRotation(Vector3f eulerRotation, int entityId) {
        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        float[] translation = new float[16];
        float[] rotation = new float[16];
        float[] scale = new float[16];
        float[] matrix = transform.localTransform.get(new float[16]);

        ImGuizmo.decomposeMatrixToComponents(matrix, translation, rotation, scale);
        rotation[0] = eulerRotation.x;
        rotation[1] = eulerRotation.y;
        rotation[2] = eulerRotation.z;
        ImGuizmo.recomposeMatrixFromComponents(translation, rotation, scale, matrix);

        transform.localTransform.set(matrix);
    }

    private Vector3f getEulerRotation(Matrix4f transform) {
        float[] translation = new float[16];
        float[] rotation = new float[16];
        float[] scale = new float[16];

        ImGuizmo.decomposeMatrixToComponents(transform.get(new float[16]), translation, rotation, scale);

        return new Vector3f(rotation[0], rotation[1], rotation[2]);
    }

    public Vector3f getWorldPosition(int entityId) {
        return getPosition(getGlobalTransform(entityId));
    }

    public Vector3f getLocalPosition(int entityId) {
        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        return getPosition(transform.localTransform);
    }

    private Vector3f getPosition(Matrix4f transform) {
        float[] translation = new float[16];
        float[] rotation = new float[16];
        float[] scale = new float[16];

        ImGuizmo.decomposeMatrixToComponents(transform.get(new float[16]), translation, rotation, scale);

        return new Vector3f(translation[0], translation[1], translation[2]);
    }

    public void setLocalPosition(Vector3f position, int entityId) {
        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        float[] translation = new float[16];
        float[] rotation = new float[16];
        float[] scale = new float[16];
        float[] matrix = transform.localTransform.get(new float[16]);

        ImGuizmo.decomposeMatrixToComponents(matrix, translation, rotation, scale);
        translation[0] = position.x;
        translation[1] = position.y;
        translation[2] = position.z;
        ImGuizmo.recomposeMatrixFromComponents(translation, rotation, scale, matrix);

        transform.localTransform.set(matrix);
    }

    public void setTransform(Matrix4f transformMatrix, int entityId) {
        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        transform.localTransform = transformMatrix;
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
