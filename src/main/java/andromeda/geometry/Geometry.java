package andromeda.geometry;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL46C.*;

public class Geometry {
    private Vertex[] vertices = {};
    private int[] indices = {};
    private int m_vao, m_vbo, m_ebo;

    public Geometry(Vector3f[] positions, int[] indices) {
        this.vertices = Arrays.stream(positions).map(Vertex::new).toArray(Vertex[]::new);
        this.indices = indices;
    }

    public Geometry(Vector3f[] positions, Vector2f[] uvs, int[] indices) {
        this.vertices = calculateVertexData(positions, new Vector3f[]{}, uvs, indices);
        this.indices = indices;
    }

    public Geometry(Vector3f[] positions, Vector3f[] normals, Vector2f[] uvs, int[] indices) {
        this.vertices = calculateVertexData(positions, normals, uvs, indices);
        this.indices = indices;
    }

    public void upload() {
        var vertex_buffer = getVertexBuffer(this.vertices);

        this.m_vao = glGenVertexArrays();
        glBindVertexArray(this.m_vao);

        this.m_vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.m_vbo);
        glBufferData(GL_ARRAY_BUFFER, vertex_buffer, GL_STATIC_DRAW);

        // position  normal    uv     tangent
        // V1 V2 V3  N1 N2 N3  U1 U2  T1 T2 T3

        int stride = Vertex.SIZE * 4;

        glVertexAttribPointer(0, Vertex.VERTEX_SIZE, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, Vertex.NORMAL_SIZE, GL_FLOAT, false, stride, 4L * Vertex.VERTEX_SIZE);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, Vertex.UV_SIZE, GL_FLOAT, false, stride, 4L * (Vertex.VERTEX_SIZE + Vertex.NORMAL_SIZE));
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, Vertex.TANGENT_SIZE, GL_FLOAT, false, stride, 4L * (Vertex.VERTEX_SIZE + Vertex.NORMAL_SIZE + Vertex.UV_SIZE));
        glEnableVertexAttribArray(3);

        if (this.indices.length != 0) {
            var indices_buffer = getIndicesBuffer(this.indices);

            this.m_ebo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.m_ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices_buffer, GL_STATIC_DRAW);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void draw() {
        glBindVertexArray(m_vao);
        if (m_ebo == 0) {
            glDrawArrays(GL_TRIANGLES, 0, this.vertices.length);
        } else {
            glDrawElements(GL_TRIANGLES, this.indices.length, GL_UNSIGNED_INT, 0L);
        }
        glBindVertexArray(0);
    }

    public void recalculateNormals() {
        Vector3f[] positions = Arrays.stream(this.vertices).map(v -> v.position).toArray(Vector3f[]::new);
        Vector2f[] uvs = Arrays.stream(this.vertices).map(v -> v.uv).toArray(Vector2f[]::new);

        this.vertices = calculateVertexData(positions, new Vector3f[]{}, uvs, this.indices);
    }

    private static Vertex[] calculateVertexData(Vector3f[] positions, Vector3f[] normals, Vector2f[] uvs, int[] indices) {
        var vertices = new Vertex[positions.length];
        boolean has_normals = normals.length != 0;

        for (int i = 0; i < vertices.length; i++) {
            var vertex = new Vertex();
            vertex.position = positions[i];
            vertex.uv = uvs[i];
            if (has_normals) {
                vertex.normal = normals[i];
            }
            vertices[i] = vertex;
        }

        for (int i = 0; i < indices.length; i += 3) {
            int i1 = indices[i];
            int i2 = indices[i + 1];
            int i3 = indices[i + 2];

            Vector2f uv0 = vertices[i1].uv;
            Vector2f uv1 = vertices[i2].uv;
            Vector2f uv2 = vertices[i3].uv;

            Vector3f p1 = vertices[i1].position;
            Vector3f p2 = vertices[i2].position;
            Vector3f p3 = vertices[i3].position;

            Vector3f e1 = p2.sub(p1, new Vector3f());
            Vector3f e2 = p3.sub(p1, new Vector3f());

            float delta_u1 = uv1.x - uv0.x;
            float delta_v1 = uv1.y - uv0.y;

            float delta_u2 = uv2.x - uv0.x;
            float delta_v2 = uv2.y - uv0.y;

            float f = 1.0f / (delta_u1 * delta_v2 - delta_u2 * delta_v1);

            Vector3f n = e1.cross(e2, new Vector3f());

            Vector3f tangent = new Vector3f();
            tangent.x = f * (delta_v2 * e1.x - delta_v1 * e2.x);
            tangent.y = f * (delta_v2 * e1.y - delta_v1 * e2.y);
            tangent.z = f * (delta_v2 * e1.z - delta_v1 * e2.z);

            Vector3f bitangent = new Vector3f();
            bitangent.x = f * (-delta_u2 * e1.x + delta_u1 * e2.x);
            bitangent.y = f * (-delta_u2 * e1.y + delta_u1 * e2.y);
            bitangent.z = f * (-delta_u2 * e1.z + delta_u1 * e2.z);

            if (!has_normals) {
                vertices[i1].normal.add(n);
                vertices[i2].normal.add(n);
                vertices[i3].normal.add(n);
            }

            vertices[i1].tangent.add(tangent);
            vertices[i2].tangent.add(tangent);
            vertices[i3].tangent.add(tangent);
        }

        for (Vertex v : vertices) {
            v.normal.normalize();
            v.tangent.normalize();
        }

        return vertices;
    }

    private FloatBuffer getVertexBuffer(Vertex[] vertices) {
        var buffer = BufferUtils.createFloatBuffer(Vertex.SIZE * vertices.length);
        for (var vertex : vertices) {
            var position = vertex.position;
            var normal = vertex.normal;
            var uv = vertex.uv;
            var tangent = vertex.tangent;
            var vertex_data = new float[]{
                    position.x, position.y, position.z,
                    normal.x, normal.y, normal.z,
                    uv.x, uv.y,
                    tangent.x, tangent.y, tangent.z};
            buffer.put(vertex_data);
        }
        buffer.flip();
        return buffer;
    }

    private IntBuffer getIndicesBuffer(int[] indices) {
        var buffer = BufferUtils.createIntBuffer(indices.length);
        buffer.put(indices);
        buffer.flip();
        return buffer;
    }

    public void setVertices(Vector3f[] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i].position = vertices[i];
        }
    }

    public Vector3f[] getVertices() {
        return Arrays.stream(this.vertices).map(v -> v.position).toArray(Vector3f[]::new);
    }
}
