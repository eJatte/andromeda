package andromeda.geometry;

import andromeda.light.Light;
import andromeda.projection.Camera;
import andromeda.shader.Program;
import org.joml.Matrix4f;

import java.util.List;

public class Model {
    private List<Mesh> meshes;

    private Matrix4f transform;

    public Model(List<Mesh> meshes, Matrix4f transform) {
        this.meshes = meshes;
        this.transform = transform;
    }

    public Model(List<Mesh> meshes) {
        this(meshes, new Matrix4f());
    }

    public Model(Mesh mesh) {
        this(List.of(mesh));
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public Matrix4f getTransform() {
        return transform;
    }
}
