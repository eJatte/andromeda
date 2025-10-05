package andromeda.ecs.component;

public class DeathTimer implements Component {

    public float timer_seconds = 10;

    @Override
    public ComponentType componentType() {
        return ComponentType.DEATH_TIMER;
    }

    @Override
    public Component createComponent() {
        return new DeathTimer();
    }
}
