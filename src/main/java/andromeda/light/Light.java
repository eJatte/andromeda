package andromeda.light;

import org.joml.Vector3f;

public class Light {
    public Vector3f position;
    public Vector3f ambient;
    public Vector3f diffuse;
    public Vector3f specular;

    public Light(Vector3f position, Vector3f color) {
        this.position = position;
        this.ambient = new Vector3f(0.5f).mul(color);
        this.diffuse = color;
        this.specular = color;
    }
}
