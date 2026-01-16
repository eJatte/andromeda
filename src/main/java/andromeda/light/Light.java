package andromeda.light;

import org.joml.Vector3f;

public abstract class Light {
    public Vector3f position;
    public Vector3f diffuse;
    public Vector3f specular;
    public float intensity;
    public boolean castShadows;

    public Light(Vector3f position, Vector3f color, float intensity) {
        this.position = position;
        this.diffuse = color;
        this.specular = color;
        this.castShadows = false;
        this.intensity = intensity;
    }

    public abstract LightType type();
}
