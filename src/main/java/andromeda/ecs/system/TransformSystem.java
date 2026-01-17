package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.component.Transform;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

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

    public TransformSystem(Ecs ecs) {
        super(ecs);
        graph = new HashMap<>();
    }

    public List<Node> getTransformHierarchy() {
        return graph.values().stream().filter(n -> n.parentNode == null).toList();
    }

    public void setParent(int entityId, int parentId) {
        var transformComp = this.ecs.getComponent(Transform.class, entityId);

        Node node = getNode(entityId);
        if (node.parentNode != null) {
            node.parentNode.children.remove(node);
        }

        Node parentNode = getNode(parentId);
        parentNode.children.add(node);
        node.parentNode = parentNode;
        transformComp.setParentEntityId(parentId);
    }

    @Override
    public void addEntity(Signature signature, int entityId) {
        if (!getEntities(signature).contains(entityId)) {
            super.addEntity(signature, entityId);

            Node node = getNode(entityId);
            var transformComp = this.ecs.getComponent(Transform.class, entityId);
            if (transformComp.getParentEntityId() != -1) {
                Node parentNode = this.getNode(transformComp.getParentEntityId());
                node.parentNode = parentNode;
                parentNode.children.add(node);
            }
        }
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);
        Node node = getNode(entityId);

        for (Node child : new ArrayList<>(node.children)) {
            ecs.destroyEntity(child.entityId);
        }

        removeNode(entityId);
        if (node.parentNode != null) {
            node.parentNode.children.remove(node);
        }
    }

    private Node getNode(int entityId) {
        if (!graph.containsKey(entityId)) {
            graph.put(entityId, new Node(entityId));
        }
        return graph.get(entityId);
    }

    private void removeNode(int entityId) {
        graph.remove(entityId);
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.TRANSFORM));
    }

    @Override
    public void update() {
    }

    public Matrix4f getGlobalTransform(int entityId) {
        var transform = ecs.getComponent(Transform.class, entityId);
        Matrix4f local = transform.getLocalTransform();
        int parentEntity = transform.getParentEntityId();
        if (parentEntity != -1) {
            return getGlobalTransform(parentEntity).mul(local, new Matrix4f());
        }
        return local;
    }

    public Vector3f getGlobalPosition(int entityId) {
        Matrix4f globalTransform = getGlobalTransform(entityId);
        return new Vector4f(0, 0, 0, 1).mul(globalTransform).xyz(new Vector3f());
    }

    public List<Integer> getChildren(int entityId) {
        return new ArrayList<>(getNode(entityId).children).stream().map(n -> n.entityId).toList();
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
