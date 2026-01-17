package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.input.Input;
import andromeda.input.KeyCode;

import java.util.Set;

public class PropertiesSystem extends EcsSystem {


    private boolean PLAY_MODE = false;

    public PropertiesSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public void init() {

    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of();
    }

    @Override
    public void update() {
        if(Input.get().keyUp(KeyCode.KEY_F2)) {
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
