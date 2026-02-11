package andromeda.scene;

import andromeda.geometry.Mesh;
import org.joml.Matrix4f;

public class RenderTarget {
    private Mesh mesh;
    private Matrix4f transform;

    private int entityId;

    public RenderTarget(Mesh mesh, Matrix4f transform, int entityId) {
        this.mesh = mesh;
        this.transform = transform;
        this.entityId = entityId;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Matrix4f getTransform() {
        return transform;
    }

    public int getEntityId() {
        return entityId;
    }
}
