package andromeda.material;

import andromeda.resources.MaterialRepresentation;
import andromeda.shader.Program;
import andromeda.texture.Texture;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Material {

    public static Vector3f DEFAULT_COLOR = new Vector3f(0.8f, 0.8f, 0.8f);
    public static Vector3f NO_COLOR = new Vector3f(-1.0f, -1.0f, -1.0f);

    public Vector3f ambient;
    public Vector3f diffuse;
    public Vector3f specular;
    public float shininess;
    public Texture diffuse_texture;
    public Texture roughness_texture;
    public Texture normal_texture;
    public Vector2f texture_scale;
    public Program program;
    public boolean isTransparent = false;

    public Material(Vector3f ambient, Vector3f diffuse, Vector3f specular, float shininess, Texture diffuse_texture, Texture normal_texture, Program program) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
        this.diffuse_texture = diffuse_texture;
        this.normal_texture = normal_texture;
        this.program = program;
        this.texture_scale = new Vector2f(1);
    }

    public Material() {
        this.ambient = Material.DEFAULT_COLOR;
        this.diffuse = Material.DEFAULT_COLOR;
        this.specular = Material.DEFAULT_COLOR;
        this.shininess = 64;
        this.diffuse_texture = null;
        this.normal_texture = null;
        this.program = null;
        this.texture_scale = new Vector2f(1);
    }

    public Material(Material material) {
        this(material.ambient, material.diffuse, material.specular, material.shininess, material.diffuse_texture, material.normal_texture, material.program);
    }

    public static Material loadMaterial(String materialPath) {
        try {
            var gson = new Gson();
            var json = FileUtils.readFileToString(new File(materialPath), StandardCharsets.UTF_8);
            var material_representation = gson.fromJson(json, MaterialRepresentation.class);

            var ambient = new Vector3f(material_representation.ambient);
            var diffuse = new Vector3f(material_representation.diffuse);
            var specular = new Vector3f(material_representation.specular);

            var texture_scale = new Vector2f(material_representation.texture_scale);

            var shininess = material_representation.shininess;

            var diffuse_texture = Optional.ofNullable(material_representation.diffuse_texture).map(Texture::loadTexture).orElse(null);
            var normal_texture = Optional.ofNullable(material_representation.normal_texture).map(Texture::loadNormalTexture).orElse(null);

            var program = Program.loadShader(material_representation.vertex_shader, material_representation.fragment_shader);

            var material = new Material(ambient, diffuse, specular, shininess, diffuse_texture, normal_texture, program);
            material.texture_scale = texture_scale;
            return material;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not load material " + materialPath);
        }
    }
}
