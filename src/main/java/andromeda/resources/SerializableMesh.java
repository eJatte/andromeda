package andromeda.resources;

import java.io.Serializable;

public record SerializableMesh(float[] vertices, float[] normals, float[] uvs, int[] indices,
                               int material_index) implements Serializable {
}
