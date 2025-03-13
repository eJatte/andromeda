package andromeda.ecs.component;

import org.joml.Vector3f;

public class DirectionalLightComponent implements Component {

    public Vector3f color;
    public Vector3f direction;
    public boolean castShadows;

    public DirectionalLightComponent() {
        this.color = new Vector3f(1, 1, 1);
        this.direction = new Vector3f(1, 1, 1).normalize();
        this.castShadows = false;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
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
