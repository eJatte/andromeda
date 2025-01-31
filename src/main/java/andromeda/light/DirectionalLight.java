package andromeda.light;

import org.joml.Vector3f;

public class DirectionalLight extends Light {

    public DirectionalLight(Vector3f direction, Vector3f color) {
        super(direction.normalize(), color);
    }

    @Override
    public LightType type() {
        return LightType.DIRECTIONAL;
    }
}
