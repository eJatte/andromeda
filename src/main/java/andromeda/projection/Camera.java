package andromeda.projection;

import andromeda.input.Input;
import andromeda.input.KeyCode;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.joml.Math.*;

public class Camera {
    private Vector3f position, targetPosition, cameraForward, targetCameraForward, cameraUp, cameraRight;

    private float pitch = -20.0f, yaw = -90.0f;

    private boolean mouseEnabled = false;

    public Camera() {
        this.position = new Vector3f(0, 4.0f, 10.0f);
        this.targetPosition = this.position;
        this.targetCameraForward = getCameraForward();
        this.cameraForward = this.targetCameraForward;
        this.cameraUp = new Vector3f(0, 1.0f, 0);
        this.cameraRight = new Vector3f(1, 0, 0);
    }

    public void update() {
        float speed = 0.05f;

        this.cameraRight = cameraForward.cross(new Vector3f(0, 1, 0), new Vector3f());
        this.cameraUp = cameraRight.cross(cameraForward, new Vector3f());

        var direction = new Vector3f(0);

        if (Input.get().key(KeyCode.KEY_W))
            direction.add(this.cameraForward);
        if (Input.get().key(KeyCode.KEY_S))
            direction.sub(this.cameraForward);

        if (Input.get().key(KeyCode.KEY_D))
            direction.add(this.cameraRight);
        if (Input.get().key(KeyCode.KEY_A))
            direction.sub(this.cameraRight);

        if (Input.get().key(KeyCode.KEY_E))
            direction.add(new Vector3f(0,1,0));
        if (Input.get().key(KeyCode.KEY_Q))
            direction.sub(new Vector3f(0,1,0));

        if (direction.length() > 0)
            direction.normalize().mul(speed);


        this.targetPosition.add(direction);
        var diffPos =  this.targetPosition.sub( this.position, new Vector3f());
        this.position.add(diffPos.mul(0.2f));

        this.targetCameraForward = getCameraForward();

        var diffFor =  this.targetCameraForward.sub( this.cameraForward, new Vector3f());
        this.cameraForward.add(diffFor.mul(0.2f));
        this.cameraForward.normalize();
    }

    private Vector3f getCameraForward() {
        var mouseDelta = mouseEnabled ? Input.get().getMouseDelta() : new Vector2f(0);

        float speed = 1.4f;

        var pitchDelta = -mouseDelta.y / 720.0f;
        pitch += pitchDelta * 40.0f * speed;
        if (pitch > 89.0f)
            pitch = 89.0f;
        if (pitch < -89.0f)
            pitch = -89.0f;

        var yawDelta = mouseDelta.x / 720.0f;
        yaw += yawDelta * 40.0f * speed;

        var front = new Vector3f(0);
        front.x = cos(toRadians(yaw)) * cos(toRadians(pitch));
        front.y = sin(toRadians(pitch));
        front.z = sin(toRadians(yaw)) * cos(toRadians(pitch));
        front.normalize();

        return front;
    }

    public Matrix4f getView() {
        return new Matrix4f().lookAt(position, position.add(cameraForward, new Vector3f()), cameraUp);
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public Matrix4f getProjection(int width, int height) {
        return new Matrix4f().setPerspective(45.0f, (float) width / (float) height, 0.1f, 100.0f);
    }

    public void setMouseEnabled(boolean mouseEnabled) {
        this.mouseEnabled = mouseEnabled;
    }
}
