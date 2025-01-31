package andromeda.resources;

import andromeda.light.DirectionalLight;
import andromeda.light.Light;
import andromeda.light.LightType;
import andromeda.light.PointLight;
import org.joml.Vector3f;

public class LightLoader {
    public static Light loadLight(LightRepresentation representation) {
        if (representation.type == LightType.DIRECTIONAL) {
            return new DirectionalLight(new Vector3f(representation.position), new Vector3f(representation.color));
        } else if (representation.type == LightType.POINT) {
            return new PointLight(new Vector3f(representation.position), new Vector3f(representation.color), representation.radius);
        } else {
            throw new IllegalArgumentException("Need to specify a light type!");
        }
    }
}
