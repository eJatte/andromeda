package andromeda.ecs.component;

import org.joml.Matrix4f;

public class Perspective implements Component {
    public float aspectRatio = 1;
    public float fov = 60;
    public float near = 0.1f, far = 100.0f;

    public Matrix4f getProjection() {
        return this.getProjection(aspectRatio);
    }

    public Matrix4f getProjection(int width, int height) {
        return new Matrix4f().setPerspective((float) Math.toRadians(fov), (float) width / (float) height, this.near, this.far);
    }

    public Matrix4f getProjection(float aspectRatio) {
        return new Matrix4f().setPerspective((float) Math.toRadians(fov), aspectRatio, this.near, this.far);
    }

    public Matrix4f getProjectionNF(float near, float far) {
        return new Matrix4f().setPerspective((float) Math.toRadians(60), aspectRatio, near, far);
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.PERSPECTIVE;
    }

    @Override
    public Component createComponent() {
        return new Perspective();
    }

    @Override
    public Component copy() {
        var comp = new Perspective();
        comp.aspectRatio = this.aspectRatio;
        comp.fov = this.fov;
        comp.near = this.near;
        comp.far = this.far;
        return comp;
    }
}
