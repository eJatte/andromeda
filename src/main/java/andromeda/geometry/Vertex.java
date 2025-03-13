package andromeda.geometry;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex {
    public static final int VERTEX_SIZE = 3, NORMAL_SIZE = 3, UV_SIZE = 2, TANGENT_SIZE = 3;
    public static final int SIZE = VERTEX_SIZE + NORMAL_SIZE + UV_SIZE + TANGENT_SIZE;

    public Vector3f position;
    public Vector3f normal;
    public Vector3f tangent;
    public Vector2f uv;

    public Vertex() {
        this.position = new Vector3f();
        this.normal = new Vector3f();
        this.tangent = new Vector3f();
        this.uv = new Vector2f();
    }

    public Vertex(Vector3f position) {
        this.position = position;
        this.normal = new Vector3f();
        this.tangent = new Vector3f();
        this.uv = new Vector2f();
    }
}
