package andromeda.resources;

import java.util.List;

public class EntityRepresentation {
    public float[] position, scale;
    public String material;
    public String geometry;
    public String name;
    public RigidBodyRepresentation rigidbody;
    public SphereColliderRepresentation sphereCollider;
    public List<EntityRepresentation> children;
}
