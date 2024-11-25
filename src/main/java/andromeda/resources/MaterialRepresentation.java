package andromeda.resources;

import java.util.Optional;

public class MaterialRepresentation {
    public float[] ambient, diffuse, specular;
    public float shininess;
    public String diffuse_texture, normal_texture;
    public String vertex_shader, fragment_shader;
}
