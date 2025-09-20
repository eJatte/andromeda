package andromeda.resources;

import org.joml.Vector3f;

import java.io.Serializable;

public record SerializableVector3f(float x, float y, float z) implements Serializable {
    public Vector3f getVector3f() {
        return new Vector3f(x, y, z);
    }
}
