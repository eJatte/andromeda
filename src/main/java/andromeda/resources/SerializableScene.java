package andromeda.resources;

import java.io.Serializable;

public record SerializableScene(SerializableModel[] models, SerializableMesh[] meshes, SerializableMaterial[] materials) implements Serializable {
}
