package andromeda.ecs.component;

import andromeda.geometry.Mesh;

import java.util.ArrayList;
import java.util.List;

public class EcsModel implements Component {
    private List<Mesh> meshes;

    public EcsModel() {
        meshes = new ArrayList<>();
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    @Override
    public ComponentType componentType() {
        return ComponentType.MODEL;
    }

    @Override
    public Component createComponent() {
        return new EcsModel();
    }
}
