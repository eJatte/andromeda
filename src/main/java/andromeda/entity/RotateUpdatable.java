package andromeda.entity;

import andromeda.input.Input;
import andromeda.input.KeyCode;
import org.joml.Vector3f;

public class RotateUpdatable implements Updatable{

    private float rotate_speed = 0.004f;

    @Override
    public void update(Entity entity) {
        if (Input.get().key(KeyCode.KEY_LEFT)) {
            entity.transform().rotate(-0.01f, new Vector3f(0, 1, 0));
        }
        if (Input.get().key(KeyCode.KEY_RIGHT)) {
            entity.transform().rotate(0.01f, new Vector3f(0, 1, 0));
        }
        if (Input.get().key(KeyCode.KEY_UP)) {
            entity.transform().rotate(0.01f, new Vector3f(1, 0, 0));
        }
        if (Input.get().key(KeyCode.KEY_DOWN)) {
            entity.transform().rotate(-0.01f, new Vector3f(1, 0, 0));
        }

        entity.transform().rotate(rotate_speed, 0, 1, 0);
        entity.transform().rotate(rotate_speed*0.5f, 1, 0, 1);
    }
}
