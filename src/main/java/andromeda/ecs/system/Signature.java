package andromeda.ecs.system;

import andromeda.ecs.component.ComponentType;
import andromeda.ecs.entity.EntityManager;

import java.util.BitSet;
import java.util.Objects;

public class Signature {

    private final BitSet signature;

    private Signature(ComponentType... componentTypes) {
        signature = getSignature(componentTypes);
        this.set(ComponentType.TRANSFORM);
    }

    public void set(ComponentType componentType) {
        signature.set(componentType.id);
    }

    public boolean has(ComponentType componentType) {
        return signature.get(componentType.id);
    }

    public boolean contains(Signature signatureOther) {
        BitSet comparison = (BitSet) this.signature.clone();
        comparison.and(signatureOther.signature);
        return signatureOther.signature.cardinality() == comparison.cardinality();
    }

    public static Signature of(ComponentType... componentTypes) {
        return new Signature(componentTypes);
    }

    public static BitSet getSignature(ComponentType... componentTypes) {
        var signature = new BitSet(EntityManager.MAX_COMPONENTS);
        for (ComponentType componentType : componentTypes) {
            signature.set(componentType.id, true);
        }
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Signature that = (Signature) o;
        return Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(signature);
    }
}
