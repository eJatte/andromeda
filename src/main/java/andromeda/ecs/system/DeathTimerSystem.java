package andromeda.ecs.system;

import andromeda.DeltaTime;
import andromeda.ecs.Ecs;
import andromeda.ecs.component.ComponentType;
import andromeda.ecs.component.DeathTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeathTimerSystem extends EcsSystem {
    public DeathTimerSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.DEATH_TIMER));
    }

    @Override
    public void update() {
        List<Integer> killed = new ArrayList<>();

        for(int entityId : this.getEntities(Signature.of(ComponentType.DEATH_TIMER))) {
            DeathTimer timer = ecs.getComponent(DeathTimer.class, entityId);
            timer.timer_seconds -= DeltaTime.deltaTime;
            if(timer.timer_seconds < 0) {
                killed.add(entityId);
            }
        }

        killed.forEach(ecs::destroyEntity);
    }

    @Override
    public SystemType type() {
        return SystemType.LOOP;
    }
}
