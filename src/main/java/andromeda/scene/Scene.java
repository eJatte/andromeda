package andromeda.scene;

import andromeda.entity.Entity;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.light.Light;
import andromeda.light.LightType;
import andromeda.projection.Camera;
import andromeda.shader.Program;
import andromeda.shader.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Entity> entities;
    private List<Light> lights;

    private final Program program;
    private final Geometry cube;

    public Scene() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public Scene(List<Entity> entities, List<Light> lights) {
        this.entities = entities;
        this.lights = lights;

        var shader = new Shader("shaders/phong.vert", "shaders/light_shader.frag");
        shader.compile();
        this.program = new Program();
        this.program.link(shader);
        shader.destroy();

        this.cube = Primitives.cube();
        cube.upload();
    }

    public void render(Camera camera) {
        entities.forEach(entity -> this.renderEntity(entity, camera));
        lights.forEach(light -> this.renderLight(light, camera));
    }

    private void renderEntity(Entity entity, Camera camera) {
        renderEntity(entity, camera, new Matrix4f());
    }

    private void renderEntity(Entity entity, Camera camera, Matrix4f parent_transform) {
        var transform = parent_transform.mul(entity.transform(), new Matrix4f());
        if (entity.model().isPresent()) {
            entity.model().get().render(camera, transform, lights);
        }

        for (var child : entity.children()) {
            renderEntity(child, camera, transform);
        }
    }

    private void renderLight(Light light, Camera camera) {
        if(light.type() != LightType.DIRECTIONAL) {
            this.program.use();
            this.program.setCamera(camera);

            var model = new Matrix4f().translate(light.position).scale(0.2f);
            this.program.setMat4("model", model);
            this.program.setVec3("color", light.diffuse);

            new Vector4f(light.position, 1).mul(new Matrix4f().rotate(0.005f, new Vector3f(0, 1, 0))).xyz(light.position);
            cube.render(this.program);
        }
    }

    public void update() {
        entities.forEach(this::updateEntity);
    }

    private void updateEntity(Entity entity) {
        entity.updatable().ifPresent(u -> u.update(entity));
        entity.children().forEach(this::updateEntity);
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
    }

    public void addLight(Light light) {
        this.lights.add(light);
    }
}
