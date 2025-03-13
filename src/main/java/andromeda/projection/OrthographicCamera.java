package andromeda.projection;

import org.joml.Matrix4f;

public class OrthographicCamera extends Camera {

    private float left, right, top, bottom;

    public OrthographicCamera(float width, float height) {
        this.left = -width;
        this.right = width;
        this.top = height;
        this.bottom = -height;
    }

    public OrthographicCamera(float left, float right, float bottom, float top) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public Matrix4f getProjection() {
        return new Matrix4f().setOrtho(left, right, bottom, top, this.getNear(), this.getFar());
    }

    public Matrix4f getProjection(float near, float far) {
        return new Matrix4f().setOrtho(left, right, bottom, top, near, far);
    }

    @Override
    public Matrix4f getProjectionWH(int width, int height) {
        return new Matrix4f().setOrtho(-width, width, -height, height, this.getNear(), this.getFar());
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getTop() {
        return top;
    }

    public float getBottom() {
        return bottom;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public void setBottom(float bottom) {
        this.bottom = bottom;
    }
}
