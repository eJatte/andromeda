package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.component.DirectionalLightComponent;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import org.joml.Vector3f;

import java.util.List;

public class LightSystem extends EcsSystem {

    public LightSystem(EcsCoordinator ecsCoordinator) {
        super(List.of(ComponentType.TRANSFORM, ComponentType.DIRECTIONAL_LIGHT), ecsCoordinator);
    }

    private Vector3f point = new Vector3f(1).normalize();
    private float horizontalValue = 0;
    private float verticalValue = 0;
    private boolean hasChanged = false;
    private float speed = 0.01f;

    @Override
    public void update() {
        if(Input.get().key(KeyCode.KEY_LEFT)) {
            horizontalValue -= speed;
            hasChanged = true;
        }
        if(Input.get().key(KeyCode.KEY_RIGHT)) {
            horizontalValue += speed;
            hasChanged = true;
        }
        if(Input.get().key(KeyCode.KEY_UP)) {
            verticalValue += speed;
            hasChanged = true;
        }
        if(Input.get().key(KeyCode.KEY_DOWN)) {
            verticalValue -= speed;
            hasChanged = true;
        }
        point.y = 0.8f + ((float) Math.sin(verticalValue));
        point.x =  ((float) Math.cos(horizontalValue));
        point.z =  ((float) Math.sin(horizontalValue));
        point.normalize();
        if(hasChanged) {
            for (int entity : entities) {
                var dirLight = ecsCoordinator.getComponent(DirectionalLightComponent.class, entity);
                dirLight.setDirection(point);
            }
        }
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
