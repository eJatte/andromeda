package andromeda.entity;

import andromeda.geometry.Geometry;
import andromeda.geometry.Mesh;
import andromeda.geometry.Model;
import andromeda.geometry.Primitives;
import andromeda.material.Material;
import andromeda.resources.EntityRepresentation;
import andromeda.resources.ModelLoader;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Entity {
    private Matrix4f transform;
    private Model model;
    private List<Entity> children;
    private Updatable updatable;

    public Entity(Model model, Updatable updatable) {
        this.model = model;
        this.transform = new Matrix4f();
        this.children = new ArrayList<>();
        this.updatable = updatable;
    }

    public Matrix4f transform() {
        return this.transform;
    }

    public Optional<Model> model() {
        return Optional.ofNullable(this.model);
    }

    public Optional<Updatable> updatable() {
        return Optional.ofNullable(this.updatable);
    }

    public List<Entity> children() {
        return this.children;
    }

    public static Entity loadEntity(String entityPath) {
        try {
            var gson = new Gson();
            var json = FileUtils.readFileToString(new File(entityPath), StandardCharsets.UTF_8);
            var entityRepresentation = gson.fromJson(json, EntityRepresentation.class);

            return loadEntity(entityRepresentation);

        } catch (IOException e) {
            throw new IllegalArgumentException("Could not load entity " + entityPath);
        }
    }

    public static Entity loadEntity(EntityRepresentation entityRepresentation) {
        var material = Material.loadMaterial(entityRepresentation.material);

        Model model = switch (entityRepresentation.geometry) {
            case "cube" -> new Model(new Mesh(Primitives.cube(), material));
            case "plane" -> new Model(new Mesh(Primitives.plane(), material));
            default -> null;
        };

        var entity = new Entity(model, (e) -> {
        });

        if (model == null)
            entity.children().add(ModelLoader.load(entityRepresentation.geometry, material));
        else
            model.getMeshes().forEach(m -> m.getGeometry().upload());

        var position = new Vector3f(entityRepresentation.position);
        var scale = new Vector3f(entityRepresentation.scale);

        entity.transform().translate(position).scale(scale);

        for (var childRep : entityRepresentation.children) {
            var child = loadEntity(childRep);
            entity.children().add(child);
        }

        return entity;
    }
}
