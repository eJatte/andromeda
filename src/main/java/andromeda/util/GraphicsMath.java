package andromeda.util;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;

public class GraphicsMath {
    public static class Sphere {
        public float radius;
        public Vector3f position;
    }

    public static Vector4f[] getFrustumCorners(Matrix4f projection, Matrix4f view) {
        var ndcCorners = getNDCCubeCorners();

        var pv = projection.mul(view, new Matrix4f());
        var pv_inverse = pv.invert(new Matrix4f());

        var frustumCorners = Arrays.stream(ndcCorners)
                .map(v -> v.mul(pv_inverse, new Vector4f()))
                .map(v -> v.mul(1.0f / v.w, new Vector4f()))
                .map(v -> new Vector4f(v.x, v.y, v.z, v.w))
                .toArray(Vector4f[]::new);

        return frustumCorners;
    }

    public static Vector4f getFrustumMidPoint(Matrix4f projection, Matrix4f view) {
        var corners = GraphicsMath.getFrustumCorners(projection, view);
        Vector4f midpoint = new Vector4f(0);
        for (Vector4f p : corners) {
            midpoint.add(p);
        }
        midpoint.x /= corners.length;
        midpoint.y /= corners.length;
        midpoint.z /= corners.length;
        midpoint.w = 1;
        return midpoint;
    }

    public static float getBoundingSphereRadius(Matrix4f projection, Matrix4f view) {
        var midpoint = GraphicsMath.getFrustumMidPoint(projection, view);
        var corners = GraphicsMath.getFrustumCorners(projection, view);

        float max_dist = 0;
        for (var corner : corners) {
            var dist = midpoint.distance(corner);
            if (dist > max_dist)
                max_dist = dist;
        }
        return max_dist;
    }

    public static Sphere getBoundingSphere(Matrix4f projection, Matrix4f view) {
        var midpoint = GraphicsMath.getFrustumMidPoint(projection, view);
        var corners = GraphicsMath.getFrustumCorners(projection, view);

        float max_dist = 0;
        for (var corner : corners) {
            var dist = midpoint.distance(corner);
            if (dist > max_dist)
                max_dist = dist;
        }

        var sphere = new Sphere();
        sphere.position = midpoint.xyz(new Vector3f());
        sphere.radius = max_dist;
        return sphere;
    }

    public static Sphere getBoundingSphereAAB(Matrix4f projection, Matrix4f view) {
        var corners = GraphicsMath.getFrustumCorners(projection, view);

        Vector3f max = corners[0].xyz(new Vector3f()), min = corners[0].xyz(new Vector3f());
        for (var corner : corners) {
            if (corner.x > max.x)
                max.x = corner.x;
            if (corner.x < min.x)
                min.x = corner.x;

            if (corner.y > max.y)
                max.y = corner.y;
            if (corner.y < min.y)
                min.y = corner.y;

            if (corner.z > max.z)
                max.z = corner.z;
            if (corner.z < min.z)
                min.z = corner.z;
        }

        Vector3f midpoint = max.sub(min, new Vector3f()).mul(0.5f).add(min);

        float max_dist = 0;
        for (var corner : corners) {
            var dist = midpoint.distance(corner.xyz(new Vector3f()));
            if (dist > max_dist)
                max_dist = dist;
        }

        var sphere = new Sphere();
        sphere.position = midpoint;
        sphere.radius = max_dist;
        return sphere;
    }

    public Vector4f[] projectFrustumOntoView(Matrix4f projection, Matrix4f view) {
        var corners = GraphicsMath.getFrustumCorners(projection, view);
        var projected_corners = new Vector4f[corners.length];
        // the max and min forms a bounding box around the projected frustum
        Vector2f max = new Vector2f(Float.MIN_VALUE), min = new Vector2f(Float.MAX_VALUE);
        for(int i = 0; i < corners.length; i++) {
            var projected = corners[i].mul(view, new Vector4f());
            projected_corners[i] = projected;
            if (projected.x > max.x)
                max.x = projected.x;
            if (projected.y > max.y)
                max.y = projected.y;
            if (projected.x < min.x)
                min.x = projected.x;
            if (projected.y < min.y)
                min.y = projected.y;
        }
        return projected_corners;
    }

    private static Vector4f[] getNDCCubeCorners() {
        return new Vector4f[]{
                // near plane
                new Vector4f(-1.0f, 1.0f, -1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, -1.0f, 1.0f),
                new Vector4f(-1.0f, -1.0f, -1.0f, 1.0f),
                new Vector4f(1.0f, -1.0f, -1.0f, 1.0f),

                // far plane
                new Vector4f(-1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(-1.0f, -1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, -1.0f, 1.0f, 1.0f),
        };
    }
}
