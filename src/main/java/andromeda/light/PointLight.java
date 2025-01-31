package andromeda.light;

import org.joml.Vector3f;

public class PointLight extends Light {
    public float radius;

    public PointLight(Vector3f position, Vector3f color, float radius) {
        super(position, color);
        this.radius = radius;
    }


    @Override
    public LightType type() {
        return LightType.POINT;
    }
}
