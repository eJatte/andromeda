package andromeda.ecs.component;

import andromeda.light.Light;
import andromeda.light.PointLight;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PointLightComponent implements Component {

    public Vector3f color;
    public float radius;
    public float intensity;

    public PointLightComponent() {
        this.color = new Vector3f(1,1,1);
        this.radius = 5;
        this.intensity = 1;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.POINT_LIGHT;
    }

    @Override
    public Component createComponent() {
        return new PointLightComponent();
    }
}
