package andromeda.ecs.component;

import org.joml.Vector3f;

public class DirectionalLightComponent implements Component {

    public Vector3f color;
    public float intensity;
    public boolean castShadows;

    public DirectionalLightComponent() {
        this.color = new Vector3f(1, 1, 1);
        this.intensity = 1;
        this.castShadows = false;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public boolean isCastShadows() {
        return castShadows;
    }

    public void setCastShadows(boolean castShadows) {
        this.castShadows = castShadows;
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.DIRECTIONAL_LIGHT;
    }

    @Override
    public Component createComponent() {
        return new DirectionalLightComponent();
    }
}
