package andromeda.resources;

import org.joml.Matrix4f;

import java.io.Serializable;

public record SerializableModel(int[] mesh_indices, int parentIndex, Matrix4f transform, String name) implements Serializable {
}
