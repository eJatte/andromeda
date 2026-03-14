package andromeda.ecs.component;

import org.joml.Vector3f;

public class SpotLightComponent implements Component {

    public Vector3f color;
    public float radius;
    public float intensity;
    public float umbra;
    public float penumbra;

    public SpotLightComponent() {
        this.color = new Vector3f(1,1,1);
        this.radius = 5;
        this.intensity = 1;
        this.umbra = 30;
        this.penumbra = 25;
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

    public float getUmbra() {
        return umbra;
    }

    public void setUmbra(float umbra) {
        this.umbra = umbra;
    }

    public float getPenumbra() {
        return penumbra;
    }

    public void setPenumbra(float penumbra) {
        this.penumbra = penumbra;
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.SPOT_LIGHT;
    }

    @Override
    public Component createComponent() {
        return new SpotLightComponent();
    }

    @Override
    public Component copy() {
        var comp = new SpotLightComponent();
        comp.color = new Vector3f(this.color);
        comp.radius = this.radius;
        comp.intensity = this.intensity;
        comp.umbra = this.umbra;
        comp.penumbra = this.penumbra;
        return comp;
    }
}
