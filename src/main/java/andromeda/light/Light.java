package andromeda.light;

import org.joml.Vector3f;

public abstract class Light {
    public Vector3f position;
    public Vector3f diffuse;
    public Vector3f specular;

    public Light(Vector3f position, Vector3f color) {
        this.position = position;
        this.diffuse = color;
        this.specular = color;
    }

    public abstract LightType type();
}
