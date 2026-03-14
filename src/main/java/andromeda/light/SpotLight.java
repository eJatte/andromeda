package andromeda.light;

import org.joml.Vector3f;

public class SpotLight extends PointLight {
    public Vector3f direction;
    public float umbra;
    public float penumbra;

    public SpotLight(Vector3f position, Vector3f direction, Vector3f color, float radius, float umbra, float penumbra, float intensity) {
        super(position, color, radius, intensity);
        this.direction = direction;
        this.umbra = umbra;
        this.penumbra = penumbra;
    }


    @Override
    public LightType type() {
        return LightType.SPOT;
    }
}
