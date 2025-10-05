package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.EcsModel;
import andromeda.ecs.component.Transform;
import andromeda.geometry.Mesh;
import andromeda.geometry.Primitives;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.material.Material;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class DebugSystem extends EcsSystem {

    enum DebugType {
        Frustum, Point
    }

    private Map<DebugType, Set<Integer>> enabledDebugEntities;
    private Map<DebugType, Queue<Integer>> disabledDebugEntities;

    public DebugSystem(Ecs ecs) {
        super(ecs);
        enabledDebugEntities = new HashMap<>();
        enabledDebugEntities.put(DebugType.Frustum, new HashSet<>());
        enabledDebugEntities.put(DebugType.Point, new HashSet<>());

        disabledDebugEntities = new HashMap<>();
        disabledDebugEntities.put(DebugType.Frustum, new LinkedList<>());
        disabledDebugEntities.put(DebugType.Point, new LinkedList<>());
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of();
    }

    @Override
    public void update() {
        if (Input.get().keyUp(KeyCode.KEY_C)) {
            disableAllDebugEntities(DebugType.Frustum);
            disableAllDebugEntities(DebugType.Point);
        }
    }

    public void disableAllDebugEntities(DebugType debugType) {
        var enabledEntities = enabledDebugEntities.get(debugType).stream().toList();
        for (var entity : enabledEntities) {
            enabledDebugEntities.get(debugType).remove(entity);
            disabledDebugEntities.get(debugType).add(entity);
            ecs.disableEntity(entity);
        }
    }

    private int getDebugEntity(DebugType debugType) {
        if (!disabledDebugEntities.get(debugType).isEmpty()) {
            var entityId = disabledDebugEntities.get(debugType).poll();
            enabledDebugEntities.get(debugType).add(entityId);
            ecs.enableEntity(entityId);
            return entityId;
        } else {
            var entityId = ecs.createEntity();
            ecs.addComponent(EcsModel.class, entityId);
            enabledDebugEntities.get(debugType).add(entityId);
            ecs.enableEntity(entityId);
            return entityId;
        }
    }

    public void createFrustumDebugEntity(Vector3f color, Matrix4f projection, Matrix4f view) {
        var frustumDebugEntity = this.getDebugEntity(DebugType.Frustum);
        var modelComponent = ecs.getComponent(EcsModel.class, frustumDebugEntity);
        if (modelComponent.getMeshes().isEmpty()) {
            var geometry = Primitives.ndcCube();
            geometry.upload();
            var material = new Material();
            material.diffuse = color;
            material.unlit = true;
            material.wireFrame = true;
            modelComponent.getMeshes().add(new Mesh(geometry, material));
        } else {
            modelComponent.getMeshes().get(0).getMaterial().diffuse = color;
        }

        var transform = ecs.getComponent(Transform.class, frustumDebugEntity);
        var pv = projection.mul(view, new Matrix4f());
        var inversePv = pv.invert(new Matrix4f());
        transform.setLocalTransform(inversePv);
    }

    public void createDebugPointEntity(Vector3f color, Vector3f point) {
        var debugEntity = this.getDebugEntity(DebugType.Point);
        var modelComponent = ecs.getComponent(EcsModel.class, debugEntity);
        if (modelComponent.getMeshes().isEmpty()) {
            var material = new Material();
            material.diffuse = color;
            material.unlit = true;

            var geometry = Primitives.icoSphere();
            geometry.upload();

            modelComponent.getMeshes().add(new Mesh(geometry, material));
        } else {
            modelComponent.getMeshes().get(0).getMaterial().diffuse = color;
        }

        var transform = ecs.getComponent(Transform.class, debugEntity);
        transform.getLocalTransform().translate(point);
        transform.getLocalTransform().scale(new Vector3f(0.1f));
    }

    public void createDebugSphereEntity(Vector3f color, Vector3f position, float radius) {
        var debugEntity = this.getDebugEntity(DebugType.Point);
        var modelComponent = ecs.getComponent(EcsModel.class, debugEntity);
        if (modelComponent.getMeshes().isEmpty()) {
            var material = new Material();
            material.diffuse = color;
            material.unlit = true;
            material.wireFrame = true;

            var geometry = Primitives.icoSphere();
            geometry.upload();

            modelComponent.getMeshes().add(new Mesh(geometry, material));
        } else {
            modelComponent.getMeshes().get(0).getMaterial().diffuse = color;
        }

        var transform = ecs.getComponent(Transform.class, debugEntity);
        transform.getLocalTransform().translate(position);
        transform.getLocalTransform().scale(new Vector3f(radius));
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
