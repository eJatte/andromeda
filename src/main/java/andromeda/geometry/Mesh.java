package andromeda.geometry;

import andromeda.light.Light;
import andromeda.material.Material;
import andromeda.projection.Camera;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Optional;

public class Mesh {
    private Geometry geometry;
    private Material material;

    public Mesh(Geometry geometry, Material material) {
        this.geometry = geometry;
        this.material = material;
    }

    public void render(Camera camera, Matrix4f model, List<Light> lights) {
        var program = material.program;

        program.use();
        program.setCamera(camera);
        program.setLights("lights", lights);

        program.setMaterial("material", material);
        program.setMat4("model", model);

        geometry.render(program);
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Material getMaterial() {
        return material;
    }
}
