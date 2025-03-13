package andromeda.ecs.component;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform implements Component {

    public Matrix4f localTransform;
    public int parentEntityId;

    public Transform(Matrix4f localTransform, int parentEntityId) {
        this.localTransform = localTransform;
        this.parentEntityId = parentEntityId;
    }

    public Transform(int parentEntityId) {
        this.parentEntityId = parentEntityId;
        this.localTransform = new Matrix4f();
    }

    public Transform() {
        this.parentEntityId = -1;
        this.localTransform = new Matrix4f();
    }

    public Vector3f getPosition() {
        return localTransform.getTranslation(new Vector3f());
    }

    public int getParentEntityId() {
        return parentEntityId;
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.TRANSFORM;
    }

    @Override
    public Component createComponent() {
        return new Transform();
    }

}
