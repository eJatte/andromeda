package andromeda.entity;

import andromeda.geometry.Geometry;
import andromeda.material.Material;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Entity {
    private Matrix4f transform;
    private Material material;
    private Geometry geometry;
    private List<Entity> children;
    private Updatable updatable;

    public Entity(Material material, Geometry geometry, Updatable updatable) {
        this.material = material;
        this.geometry = geometry;
        this.transform = new Matrix4f();
        this.children = new ArrayList<>();
        this.updatable = updatable;
    }

    public Matrix4f transform() {
        return this.transform;
    }

    public Optional<Material> material() {
        return Optional.ofNullable(this.material);
    }

    public Optional<Geometry> geometry() {
        return Optional.ofNullable(this.geometry);
    }

    public Optional<Updatable> updatable() {
        return Optional.ofNullable(this.updatable);
    }

    public List<Entity> children() {
        return this.children;
    }


}
