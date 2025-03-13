package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.input.Input;
import andromeda.input.KeyCode;

import java.util.List;

public class PropertiesSystem extends EcsSystem {


    private boolean PLAY_MODE = false;

    public PropertiesSystem(EcsCoordinator ecsCoordinator) {
        super(List.of(), ecsCoordinator);
    }

    @Override
    public void init() {

    }

    @Override
    public void update() {
        if(Input.get().keyUp(KeyCode.KEY_2)) {
            togglePlayMode();
        }
    }

    private void togglePlayMode() {
        PLAY_MODE = !PLAY_MODE;
    }

    public boolean isPlayMode() {
        return PLAY_MODE;
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
