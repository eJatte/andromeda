package andromeda.ecs.system;

import andromeda.DeltaTime;
import andromeda.ecs.Ecs;
import andromeda.ecs.component.*;
import andromeda.geometry.Mesh;
import andromeda.geometry.Model;
import andromeda.geometry.Primitives;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.material.Material;
import org.joml.Vector3f;

import java.util.Random;
import java.util.Set;

public class PhysicsSystem extends EcsSystem {
    public PhysicsSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(ComponentType.TRANSFORM, ComponentType.RIGID_BODY));
    }

    @Override
    public void update() {
        for (int entityId : this.getEntities(Signature.of(ComponentType.TRANSFORM, ComponentType.RIGID_BODY))) {
            updateEntity(entityId);
        }

        if (Input.get().keyUp(KeyCode.KEY_K)) {
            ecs.addComponent(RigidBody.class, 1);
        }
        if(Input.get().keyUp(KeyCode.KEY_L)) {
            RigidBody rigidBody = ecs.getComponent(RigidBody.class, 1);
            Random random = new Random();

            rigidBody.velocity = new Vector3f(random.nextInt() % 20,  Math.abs(random.nextInt() % 10) + 10, random.nextInt() % 20).normalize().mul(10);

            System.out.println(rigidBody.velocity.x);
        }

        if(Input.get().key(KeyCode.KEY_H)) {
            Random random = new Random();

            int entityId = ecs.createEntity();
            EcsModel ecsModel = ecs.addComponent(EcsModel.class, entityId);


            var material = new Material();
            float r = random.nextFloat() * 0.6f + 0.2f;
            float g = random.nextFloat() * 0.6f + 0.2f;
            float b = random.nextFloat() * 0.6f + 0.2f;
            Vector3f color = new Vector3f(r,g,b);

            material.diffuse = color;
            material.specular = color;
            material.ambient = color;

            Model model = new Model(new Mesh(Primitives.cube(), material));

            ecsModel.getMeshes().addAll(model.getMeshes());
            ecsModel.getMeshes().forEach(m -> m.getGeometry().upload());

            Transform transform = ecs.getComponent(Transform.class, entityId);
            transform.setName("cube");
            transform.setPosition(new Vector3f(0, 0.5f, 0));

            RigidBody rigidBody = ecs.addComponent(RigidBody.class, entityId);

            float x = random.nextFloat() * 10 + 5;
            float y = random.nextFloat() * 20 + 5;
            float z = random.nextFloat() * 10 + 5;

            rigidBody.velocity = new Vector3f(x, y, z);

            DeathTimer timer = ecs.addComponent(DeathTimer.class, entityId);
            timer.timer_seconds = random.nextInt() % 20 + 3f;
        }
    }

    private void updateEntity(int entityId) {
        Transform transform = ecs.getComponent(Transform.class, entityId);
        RigidBody rigidBody = ecs.getComponent(RigidBody.class, entityId);

        float gravity = 9.82f;
        Vector3f acceleration = new Vector3f(0, -1 * gravity * DeltaTime.deltaTime, 0);
        rigidBody.velocity.add(acceleration);

        float dragForceMagnitude = rigidBody.velocity.lengthSquared() * rigidBody.drag;
        Vector3f dragForceVector = rigidBody.velocity.normalize(new Vector3f()).negate().mul(dragForceMagnitude * DeltaTime.deltaTime);

        rigidBody.velocity.add(dragForceVector);

        Vector3f pos = new Vector3f(transform.getPosition());

        if (pos.y < 0.5f) {
            rigidBody.velocity = new Vector3f(0);
            transform.setPosition(new Vector3f(pos.x, 0.5f, pos.z));
        } else {
            Vector3f translation = new Vector3f(rigidBody.velocity);
            translation.mul(DeltaTime.deltaTime);
            transform.translate(translation);
        }
    }

    @Override
    public SystemType type() {
        return SystemType.PHYSICS;
    }
}
