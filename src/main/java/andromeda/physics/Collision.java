package andromeda.physics;

import org.joml.Vector3f;

public record Collision(Vector3f normal, float depth, Vector3f contactPoint, int entity_a, int entity_b) {

}
