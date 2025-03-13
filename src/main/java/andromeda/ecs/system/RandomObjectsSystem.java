package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.EcsModel;
import andromeda.ecs.component.Transform;
import andromeda.geometry.Geometry;
import andromeda.geometry.Mesh;
import andromeda.geometry.Primitives;
import andromeda.material.Material;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;

public class RandomObjectsSystem extends EcsSystem {

    private Geometry cube;


    public RandomObjectsSystem(EcsCoordinator ecsCoordinator) {
        super(List.of(), ecsCoordinator);
    }

    @Override
    public void init() {
        cube = Primitives.cube();
        cube.upload();
        for(int i = 0; i < 400; i++)
            createRandomObject();
    }

    private void createRandomObject() {
        int entityId = ecsCoordinator.createEntity();
        var modelComponent = ecsCoordinator.addComponent(EcsModel.class, entityId);
        var material = new Material();
        var random = new Random();
        material.diffuse = new Vector3f(random.nextFloat() % 0.7f + 0.1f,random.nextFloat() % 0.7f + 0.1f,random.nextFloat() % 0.7f + 0.1f);
        material.specular = new Vector3f(0.7f);
        material.ambient = new Vector3f(material.diffuse);

        modelComponent.getMeshes().add(new Mesh(cube, material));

        var transform = ecsCoordinator.getComponent(Transform.class, entityId);
        transform.localTransform.translate(new Vector3f(random.nextFloat() * 200.0f - 100, random.nextFloat() * 10.0f , random.nextFloat() * 200.0f - 100));
        float scale = random.nextFloat() * 5.0f + 0.2f;
        transform.localTransform.scale(new Vector3f(scale - (random.nextFloat() * scale * 0.3f), scale - (random.nextFloat() * scale * 0.3f), scale - (random.nextFloat() * scale * 0.3f)));
        Quaternionf quaternionf = new Quaternionf().fromAxisAngleDeg(new Vector3f(random.nextFloat(), random.nextFloat() ,random.nextFloat()).normalize(), random.nextFloat() * 360);
        transform.localTransform.rotate(quaternionf);
    }

    @Override
    public void update() {

    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
