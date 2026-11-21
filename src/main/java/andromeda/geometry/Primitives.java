package andromeda.geometry;

import org.apache.commons.lang3.ArrayUtils;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Arrays;

public class Primitives {

    public static Geometry quad() {
        var vertices = new Vector3f[]{
                new Vector3f(-1.0f, 1.0f, 0.0f),
                new Vector3f(1.0f, 1.0f, 0.0f),
                new Vector3f(1.0f, -1.0f, 0.0f),
                new Vector3f(-1.0f, -1.0f, 0.0f),
        };

        var uvs = new Vector2f[]{
                new Vector2f(0, 1),
                new Vector2f(1, 1),
                new Vector2f(1, 0),
                new Vector2f(0, 0),
        };

        var indices = new int[]{
                0, 3, 2,
                0, 2, 1,
        };
        return new Geometry(vertices, uvs, indices);
    }

    public static Geometry ndcCube() {
        var vertices = new Vector3f[]{
                // near plane
                new Vector3f(-1.0f, 1.0f, -1.0f),
                new Vector3f(1.0f, 1.0f, -1.0f),
                new Vector3f(1.0f, -1.0f, -1.0f),
                new Vector3f(-1.0f, -1.0f, -1.0f),

                // far plane
                new Vector3f(-1.0f, 1.0f, 1.0f),
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Vector3f(1.0f, -1.0f, 1.0f),
                new Vector3f(-1.0f, -1.0f, 1.0f),
        };

        var indices = new int[]{
                // near
                0, 1, 2,
                0, 2, 3,
                // far
                4, 6, 5,
                4, 7, 6,
                // left
                4, 0, 3,
                4, 3, 7,
                // right
                1, 5, 6,
                1, 6, 2,
                // top
                4, 5, 1,
                4, 1, 0,
                // bottom
                3, 2, 6,
                3, 6, 7

        };

        return new Geometry(vertices, indices);
    }

    public static Geometry identity() {
        var vertices = new Vector3f[]{
                // front
                new Vector3f(-1.0f, 1.0f, 1.0f),
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Vector3f(1.0f, -1.0f, 1.0f),
                new Vector3f(-1.0f, -1.0f, 1.0f),

                // back
                new Vector3f(-1.0f, 1.0f, -1.0f),
                new Vector3f(1.0f, 1.0f, -1.0f),
                new Vector3f(1.0f, -1.0f, -1.0f),
                new Vector3f(-1.0f, -1.0f, -1.0f),

                // left
                new Vector3f(-1.0f, 1.0f, 1.0f),
                new Vector3f(-1.0f, 1.0f, -1.0f),
                new Vector3f(-1.0f, -1.0f, -1.0f),
                new Vector3f(-1.0f, -1.0f, 1.0f),

                // right
                new Vector3f(1.0f, 1.0f, 1.0f),
                new Vector3f(1.0f, 1.0f, -1.0f),
                new Vector3f(1.0f, -1.0f, -1.0f),
                new Vector3f(1.0f, -1.0f, 1.0f),

                // top
                new Vector3f(-1.0f, 1.0f, 1.0f),
                new Vector3f(-1.0f, 1.0f, -1.0f),
                new Vector3f(1.0f, 1.0f, -1.0f),
                new Vector3f(1.0f, 1.0f, 1.0f),

                // bot
                new Vector3f(-1.0f, -1.0f, 1.0f),
                new Vector3f(-1.0f, -1.0f, -1.0f),
                new Vector3f(1.0f, -1.0f, -1.0f),
                new Vector3f(1.0f, -1.0f, 1.0f),
        };

        var uvs = new Vector2f[]{
                // front
                new Vector2f(0, 1),
                new Vector2f(1, 1),
                new Vector2f(1, 0),
                new Vector2f(0, 0),

                // back
                new Vector2f(1, 1),
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),

                // left
                new Vector2f(1, 1),
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),

                // right
                new Vector2f(0, 1),
                new Vector2f(1, 1),
                new Vector2f(1, 0),
                new Vector2f(0, 0),

                // top
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),
                new Vector2f(1, 1),


                // bot
                new Vector2f(1, 1),
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),
        };

        var indices = new int[]{
                // front
                0, 3, 2,
                0, 2, 1,

                // back
                4, 6, 7,
                4, 5, 6,

                // left
                8, 10, 11,
                8, 9, 10,

                // right
                12, 15, 14,
                12, 14, 13,

                // top
                16, 18, 17,
                16, 19, 18,

                // bot
                20, 21, 22,
                20, 22, 23,
        };
        return new Geometry(vertices, uvs, indices);
    }

    public static Geometry icoSphere() {
        float X = 0.5257311f;
        float Z = 0.8506508f;

        var vertices = new Vector3f[]{
                new Vector3f(-X, 0, Z), new Vector3f(X, 0, Z),
                new Vector3f(-X, 0, -Z), new Vector3f(X, 0, -Z),
                new Vector3f(0, Z, X), new Vector3f(0, Z, -X),
                new Vector3f(0, -Z, X), new Vector3f(0, -Z, -X),
                new Vector3f(Z, X, 0), new Vector3f(-Z, X, 0),
                new Vector3f(Z, -X, 0), new Vector3f(-Z, -X, 0),
        };

        // Simple UV mapping using spherical coordinates
        var uvs = new Vector2f[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            Vector3f v = vertices[i];
            float u = (float) (0.5 + Math.atan2(v.z, v.x) / (2 * Math.PI));
            float vCoord = (float) (0.5 - Math.asin(v.y) / Math.PI);
            uvs[i] = new Vector2f(u, vCoord);
        }

        var indices = new int[]{
                0, 4, 1, 0, 9, 4, 9, 5, 4, 4, 5, 8, 4, 8, 1,
                8, 10, 1, 8, 3, 10, 5, 3, 8, 5, 2, 3, 2, 7, 3,
                7, 10, 3, 7, 6, 10, 7, 11, 6, 11, 0, 6, 0, 1, 6,
                6, 1, 10, 9, 0, 11, 9, 11, 2, 9, 2, 5, 7, 2, 11
        };

        return new Geometry(vertices, uvs, indices);
    }

    public static Geometry cube() {
        var vertices = new Vector3f[]{
                // front
                new Vector3f(-0.5f, 0.5f, 0.5f),
                new Vector3f(0.5f, 0.5f, 0.5f),
                new Vector3f(0.5f, -0.5f, 0.5f),
                new Vector3f(-0.5f, -0.5f, 0.5f),

                // back
                new Vector3f(-0.5f, 0.5f, -0.5f),
                new Vector3f(0.5f, 0.5f, -0.5f),
                new Vector3f(0.5f, -0.5f, -0.5f),
                new Vector3f(-0.5f, -0.5f, -0.5f),

                // left
                new Vector3f(-0.5f, 0.5f, 0.5f),
                new Vector3f(-0.5f, 0.5f, -0.5f),
                new Vector3f(-0.5f, -0.5f, -0.5f),
                new Vector3f(-0.5f, -0.5f, 0.5f),

                // right
                new Vector3f(0.5f, 0.5f, 0.5f),
                new Vector3f(0.5f, 0.5f, -0.5f),
                new Vector3f(0.5f, -0.5f, -0.5f),
                new Vector3f(0.5f, -0.5f, 0.5f),

                // top
                new Vector3f(-0.5f, 0.5f, 0.5f),
                new Vector3f(-0.5f, 0.5f, -0.5f),
                new Vector3f(0.5f, 0.5f, -0.5f),
                new Vector3f(0.5f, 0.5f, 0.5f),

                // bot
                new Vector3f(-0.5f, -0.5f, 0.5f),
                new Vector3f(-0.5f, -0.5f, -0.5f),
                new Vector3f(0.5f, -0.5f, -0.5f),
                new Vector3f(0.5f, -0.5f, 0.5f),
        };

        var uvs = new Vector2f[]{
                // front
                new Vector2f(0, 1),
                new Vector2f(1, 1),
                new Vector2f(1, 0),
                new Vector2f(0, 0),

                // back
                new Vector2f(1, 1),
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),

                // left
                new Vector2f(1, 1),
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),

                // right
                new Vector2f(0, 1),
                new Vector2f(1, 1),
                new Vector2f(1, 0),
                new Vector2f(0, 0),

                // top
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),
                new Vector2f(1, 1),


                // bot
                new Vector2f(1, 1),
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),
        };

        var indices = new int[]{
                // front
                0, 3, 2,
                0, 2, 1,

                // back
                4, 6, 7,
                4, 5, 6,

                // left
                8, 10, 11,
                8, 9, 10,

                // right
                12, 15, 14,
                12, 14, 13,

                // top
                16, 18, 17,
                16, 19, 18,

                // bot
                20, 21, 22,
                20, 22, 23,
        };
        return new Geometry(vertices, uvs, indices);
    }

    public static Geometry plane() {
        var vertices = new Vector3f[]{
                // top
                new Vector3f(-0.5f, 0.0f, 0.5f),
                new Vector3f(-0.5f, 0.0f, -0.5f),
                new Vector3f(0.5f, 0.0f, -0.5f),
                new Vector3f(0.5f, 0.0f, 0.5f),
        };

        var uvs = new Vector2f[]{
                // top
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0),
                new Vector2f(1, 1),
        };

        var indices = new int[]{
                // front
                0, 3, 2,
                0, 2, 1,
        };
        return new Geometry(vertices, uvs, indices);
    }

    public static Geometry wobbly_grid(int x_size, int y_size, int resolution, float amplitude, float frequency) {
        var grid = grid(x_size, y_size, resolution);

        var vertices = Arrays.stream(grid.getVertices()).map(v -> {
            return new Vector3f(v.x, (float) Math.sin(v.x * frequency) * amplitude + (float) Math.cos(v.z * frequency) * amplitude, v.z);
        }).toArray(Vector3f[]::new);

        grid.setVertices(vertices);

        grid.recalculateNormals();

        return grid;
    }

    public static Geometry grid(int x_size, int y_size, int resolution) {
        Vector2i grid_size = new Vector2i(x_size * resolution, y_size * resolution);
        Vector2i v_size = new Vector2i(1, 1).add(grid_size);

        Vector3f[] vertices = new Vector3f[v_size.x * v_size.y];
        Vector2f[] uvs = new Vector2f[v_size.x * v_size.y];

        for (int x = 0; x < v_size.x; x++) {
            for (int y = 0; y < v_size.y; y++) {
                Vector2f pos = new Vector2f(x / (float) resolution, y / (float) resolution);
                int index = getIndex(x, y, v_size);
                vertices[index] = new Vector3f(pos.x, 0, pos.y);
                uvs[index] = new Vector2f(pos.x, pos.y);
            }
        }

        int[] indices = new int[grid_size.x * 6 * grid_size.y];

        for (int x = 0; x < grid_size.x; x++) {
            for (int y = 0; y < grid_size.y; y++) {
                int vi1 = getIndex(x, y, v_size);
                int vi2 = getIndex(x + 1, y, v_size);
                int vi3 = getIndex(x, y + 1, v_size);
                int vi4 = getIndex(x + 1, y + 1, v_size);

                int i = getIndex(x * 6, y, new Vector2i(grid_size.x * 6, grid_size.y));

                indices[i] = vi1;
                indices[i + 1] = vi4;
                indices[i + 2] = vi2;

                indices[i + 3] = vi1;
                indices[i + 4] = vi3;
                indices[i + 5] = vi4;
            }
        }

        return new Geometry(vertices, uvs, indices);
    }

    public static Geometry circle() {
        return circle(80);
    }

    public static Geometry circle(int segments) {
        Vector3f[] vertices = new Vector3f[segments];
        for (int i = 0; i < segments; i++) {
            float angle = (float) Math.PI * 2 / segments * i;
            vertices[i] = new Vector3f((float) Math.cos(angle), (float) Math.sin(angle), 0);
        }
        return new Geometry(vertices);
    }

    public static Geometry cone() {
        return cone(80);
    }

    public static Geometry cone(int segments) {
        // we need to double the bottom plate vertices since we want different normals for the top and bottom faces
        Vector3f[] vertices = new Vector3f[segments * 2 + 1 + segments];
        Vector3f[] normals = new Vector3f[segments * 2 + 1 + segments];

        Vector3f bot_normal = new Vector3f(0, -1, 0);

        int top_offset = (segments + 1);

        for (int i = 0; i < segments; i++) {
            float angle = (float) Math.PI * 2 / segments * i;
            float radius = 0.5f;
            Vector3f position = new Vector3f((float) Math.cos(angle) * radius, 0, (float) Math.sin(angle) * radius);

            vertices[i] = position;
            normals[i] = bot_normal;

            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f normal = position.add(up, new Vector3f());

            vertices[top_offset + i] = position;
            normals[top_offset + i] = normal;
            // we need to add top position for each segment because the normal needs to be different for each face
            vertices[top_offset + segments + i] = up;
            normals[top_offset + segments + i] = normal;
        }

        // bottom can be "constant" since it is a flat plane
        vertices[segments] = new Vector3f(0); // bottom
        normals[segments] = bot_normal;


        int[] indices = new int[segments * 6];

        for (int i = 0; i < segments; i++) {
            int offset = 6 * i;
            // bottom segment
            indices[offset + 0] = i + 0;
            indices[offset + 1] = (i + 1) % segments;
            indices[offset + 2] = segments;

            // top segment
            // different winding order since the faces are in different directions
            indices[offset + 3] = top_offset + i + 0;
            indices[offset + 4] = top_offset + segments + i;
            indices[offset + 5] = top_offset + (i + 1) % segments;
        }

        return new Geometry(vertices, normals, indices);
    }


    public static Geometry line() {
        return new Geometry(new Vector3f[]{new Vector3f(0), new Vector3f(0, 0, 1)});
    }

    public static int getIndex(int x, int y, Vector2i v_size) {
        return x + y * v_size.x;
    }
}
