package andromeda.projection;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Camera {
    protected Vector3f position, cameraForward, cameraUp;

    protected float near, far;

    public Camera() {
        this.position = new Vector3f(-11.0f, 3.0f, 0.0f);
        this.cameraForward = new Vector3f(0, 0.0f, -1.0f);
        this.cameraUp = new Vector3f(0, 1.0f, 0);
        this.near = 0.1f;
        this.far = 100.0f;
    }

    public Matrix4f getView() {
        return new Matrix4f().lookAt(this.getPosition(), this.getPosition().add(this.getCameraForward(), new Vector3f()), this.getCameraUp());
    }

    public abstract Matrix4f getProjection();

    public abstract Matrix4f getProjection(float near, float far);

    public abstract Matrix4f getProjectionWH(int width, int height);

    public Matrix4f getProjectionView() {
        return this.getProjection().mul(this.getView());
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setCameraForward(Vector3f cameraForward) {
        this.cameraForward = cameraForward;
    }

    public void setCameraUp(Vector3f cameraUp) {
        this.cameraUp = cameraUp;
    }

    public void lookAtPoint(Vector3f point) {
        var diff = point.sub(this.position, new Vector3f());
        this.cameraForward = diff.normalize();
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public Vector3f getCameraForward() {
        return cameraForward;
    }

    public Vector3f getCameraUp() {
        return cameraUp;
    }

    public float getNear() {
        return near;
    }

    public void setNear(float near) {
        this.near = near;
    }

    public float getFar() {
        return far;
    }

    public void setFar(float far) {
        this.far = far;
    }
}
