package andromeda.util;

import org.joml.Matrix4f;

public class Cascade {
    public Matrix4f lightSpaceProjection;
    public float distance;
    public float size;

    public Cascade(Matrix4f lightSpaceProjection, float distance, float size) {
        this.lightSpaceProjection = lightSpaceProjection;
        this.distance = distance;
        this.size = size;
    }
}
