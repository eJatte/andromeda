package andromeda.render.pipeline;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.EcsModel;
import andromeda.ecs.system.TransformSystem;
import andromeda.geometry.Mesh;
import andromeda.scene.RenderTarget;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CullPass {

    private Ecs ecs;

    private TransformSystem transformSystem;

    public CullPass(Ecs ecs) {
        this.ecs = ecs;
    }

    public void init() {
        transformSystem = ecs.getSystem(TransformSystem.class);
    }

    public List<RenderTarget> cullRenderTargets(Set<Integer> entities) {
        return entities.stream().map(this::getRenderTarget).flatMap(Collection::stream).toList();
    }

    private List<RenderTarget> getRenderTarget(int entityId) {
        EcsModel ecsModel = ecs.getComponent(EcsModel.class, entityId);
        Matrix4f transform = transformSystem.getGlobalTransform(entityId);
        return ecsModel.getMeshes().stream().map(mesh -> getRenderTarget(mesh, transform)).toList();
    }

    private RenderTarget getRenderTarget(Mesh mesh, Matrix4f transform) {
        return new RenderTarget(mesh, transform);
    }

}
