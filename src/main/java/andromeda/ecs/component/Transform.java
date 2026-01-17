package andromeda.ecs.component;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static andromeda.util.GraphicsMath.DEG2RAD;
import static andromeda.util.GraphicsMath.RAD2DEG;

public class Transform implements Component {



    private Matrix4f localTransform;
    private Vector3f position;
    private Quaternionf rotation;
    private Vector3f scale;
    private int parentEntityId;
    private boolean dirtyFlag;
    private String name;

    public Transform() {
        this.localTransform = new Matrix4f();

        this.position = new Vector3f(0);
        this.rotation = new Quaternionf();
        this.scale = new Vector3f(1);

        this.parentEntityId = -1;
        this.dirtyFlag = true;

        this.name = null;
    }

    public int getParentEntityId() {
        return parentEntityId;
    }

    public Matrix4f getLocalTransform() {
        if (dirtyFlag) {
            dirtyFlag = false;
            localTransform.set(this.getLocalTransformMatrix());
        }
        return localTransform;
    }


    private float[] getLocalTransformMatrix() {
        Matrix4f localTransform = new Matrix4f().translation(position).rotate(rotation).scale(scale);
        return localTransform.get(new float[16]);
    }

    public void setLocalTransform(Matrix4f localTransform) {
        this.setLocalTransform(localTransform.get(new float[16]));
    }

    public void setLocalTransform(float[] matrix) {
        Matrix4f m = new Matrix4f().set(matrix);
        m.getUnnormalizedRotation(this.rotation);
        m.getTranslation(this.position);
        m.getScale(this.scale);

        dirtyFlag = true;
    }

    public void setParentEntityId(int parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public void setPosition(Vector3f position) {
        dirtyFlag = true;
        this.position = position;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setScale(Vector3f scale) {
        dirtyFlag = true;
        this.scale = scale;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void rotateEuler(Vector3f eulerRotation) {
        dirtyFlag = true;
        float x = eulerRotation.x % 360;
        float y = eulerRotation.y % 360;
        float z = eulerRotation.z % 360;
        Quaternionf change = new Quaternionf();
        change.rotateXYZ(x * DEG2RAD, y * DEG2RAD, z * DEG2RAD);
        rotation.mul(change);
    }

    public Vector3f getEulerRotation() {
        return this.rotation.getEulerAnglesXYZ(new Vector3f()).mul(RAD2DEG);
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public Quaternionf rotateSlerp(Quaternionf q, float alpha) {
        dirtyFlag = true;
        return this.rotation.slerp(q, alpha);
    }

    public Quaternionf rotate(Quaternionf quaternionf) {
        dirtyFlag = true;
        return rotation.mul(quaternionf);
    }

    public void translate(Vector3f translation) {
        dirtyFlag = true;
        this.position.add(translation, this.position);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.TRANSFORM;
    }

    @Override
    public Component createComponent() {
        return new Transform();
    }

    @Override
    public Component copy() {
        var comp = new Transform();
        comp.localTransform = new Matrix4f(this.localTransform);
        comp.position = new Vector3f(this.position);
        comp.rotation = new Quaternionf(this.rotation);
        comp.scale = new Vector3f(this.scale);
        comp.parentEntityId = this.parentEntityId;
        comp.dirtyFlag = this.dirtyFlag;
        comp.name = this.name;
        return comp;
    }

}
