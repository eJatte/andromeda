package andromeda.projection;

import andromeda.ecs.component.Perspective;
import org.joml.Matrix4f;

public class EcsCamera extends Camera {

    public Perspective perspective;
    public Matrix4f view;

    public EcsCamera(Perspective perspective, Matrix4f view) {
        this.perspective = perspective;
        this.view = view;
    }

    @Override
    public Matrix4f getView() {
        return view;
    }

    @Override
    public Matrix4f getProjection() {
        return perspective.getProjection();
    }

    @Override
    public Matrix4f getProjection(float near, float far) {
        return perspective.getProjectionNF(near, far);
    }

    @Override
    public Matrix4f getProjectionWH(int width, int height) {
        return perspective.getProjection(width, height);
    }
}
