package andromeda.geometry;

import andromeda.light.Light;
import andromeda.projection.Camera;
import andromeda.shader.Program;
import org.joml.Matrix4f;

import java.util.List;

public class Model {
    private List<Mesh> meshes;

    public Model(List<Mesh> meshes) {
        this.meshes = meshes;
    }

    public Model(Mesh mesh) {
        this(List.of(mesh));
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }
}
