package andromeda.scene;

import andromeda.geometry.Mesh;
import org.joml.Matrix4f;

public class RenderTarget {
    private Mesh mesh;
    private Matrix4f transform;

    public RenderTarget(Mesh mesh, Matrix4f transform) {
        this.mesh = mesh;
        this.transform = transform;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Matrix4f getTransform() {
        return transform;
    }
}
