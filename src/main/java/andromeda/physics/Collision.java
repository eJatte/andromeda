package andromeda.physics;

import org.joml.Vector3f;

public record Collision(Vector3f normal, float depth, int entity_a, int entity_b) {

}
