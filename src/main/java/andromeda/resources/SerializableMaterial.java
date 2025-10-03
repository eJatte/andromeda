package andromeda.resources;

import java.io.Serializable;

public record SerializableMaterial(String diffusePath, String normalPath, String roughnessPath,
                                   SerializableVector3f diffuseColor, SerializableVector3f ambientColor,
                                   SerializableVector3f specularColor, float shininess) implements Serializable {
}
