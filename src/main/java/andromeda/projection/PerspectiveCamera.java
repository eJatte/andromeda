package andromeda.projection;

import org.joml.Matrix4f;

public class PerspectiveCamera extends Camera {

    private final int width, height;

    public PerspectiveCamera(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Matrix4f getProjection() {
        return new Matrix4f().setPerspective((float) Math.toRadians(60), (float) width / (float) height, this.getNear(), this.getFar());
    }

    public Matrix4f getProjectionWH(int width, int height) {
        return new Matrix4f().setPerspective((float) Math.toRadians(60), (float) width / (float) height, this.getNear(), this.getFar());
    }

    public Matrix4f getProjection(float near, float far) {
        return new Matrix4f().setPerspective((float) Math.toRadians(60), (float) width / (float) height, near, far);
    }
}
