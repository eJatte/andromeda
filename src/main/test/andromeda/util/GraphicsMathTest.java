package andromeda.util;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

class GraphicsMathTest {

    @Test
    void shouldGetCorrectFrustumCornersForOrthographicProjection() {
        var projection = new Matrix4f().ortho(-10, 10, -10, 10, -2, 10);
        var view = new Matrix4f().identity();

        var expectedCorners = new Vector4f[]{
                // near plane
                new Vector4f(-10.0f, 10.0f, -2.0f, 1.0f),
                new Vector4f(10.0f, 10.0f, -2.0f, 1.0f),
                new Vector4f(-10.0f, -10.0f, -2.0f, 1.0f),
                new Vector4f(10.0f, -10.0f, -2.0f, 1.0f),

                // far plane
                new Vector4f(-10.0f, 10.0f, 10.0f, 1.0f),
                new Vector4f(10.0f, 10.0f, 10.0f, 1.0f),
                new Vector4f(-10.0f, -10.0f, 10.0f, 1.0f),
                new Vector4f(10.0f, -10.0f, 10.0f, 1.0f),
        };

        var frustumCorners = GraphicsMath.getFrustumCorners(projection, view);

        assertCornersEquals(expectedCorners, frustumCorners, 0.0001f);
    }


    @Test
    void shouldGetCorrectFrustumCornersForPerspectiveProjection() {
        var projection = new Matrix4f().setPerspective((float) Math.toRadians(90), 1, 2.0f, 5.0f);
        var view = new Matrix4f().identity();

        var expectedCorners = new Vector4f[]{
                // near plane
                new Vector4f(-2.0f, 2.0f, 2.0f, 1.0f),
                new Vector4f(2.0f, 2.0f, 2.0f, 1.0f),
                new Vector4f(-2.0f, -2.0f, 2.0f, 1.0f),
                new Vector4f(2.0f, -2.0f, 2.0f, 1.0f),

                // far plane
                new Vector4f(-5.0f, 5.0f, 5.0f, 1.0f),
                new Vector4f(5.0f, 5.0f, 5.0f, 1.0f),
                new Vector4f(-5.0f, -5.0f, 5.0f, 1.0f),
                new Vector4f(5.0f, -5.0f, 5.0f, 1.0f),
        };

        var frustumCorners = GraphicsMath.getFrustumCorners(projection, view);

        assertCornersEquals(expectedCorners, frustumCorners, 0.0001f);
    }

    @Test
    void shouldGetCorrectFrustumCornersForPerspectiveProjectionAnotherAngle() {
        var projection = new Matrix4f().setPerspective((float) Math.toRadians(26.5650512 * 2.0f), 1, 2.0f, 5.0f);
        var view = new Matrix4f().identity();

        var expectedCorners = new Vector4f[]{
                // near plane
                new Vector4f(-1.0f, 1.0f, 2.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 2.0f, 1.0f),
                new Vector4f(-1.0f, -1.0f, 2.0f, 1.0f),
                new Vector4f(1.0f, -1.0f, 2.0f, 1.0f),

                // far plane
                new Vector4f(-2.5f, 2.5f, 5.0f, 1.0f),
                new Vector4f(2.5f, 2.5f, 5.0f, 1.0f),
                new Vector4f(-2.5f, -2.5f, 5.0f, 1.0f),
                new Vector4f(2.5f, -2.5f, 5.0f, 1.0f),
        };

        var frustumCorners = GraphicsMath.getFrustumCorners(projection, view);

        assertCornersEquals(expectedCorners, frustumCorners, 0.0001f);
    }

    private void assertCornersEquals(Vector4f[] expected, Vector4f[] actual, float epsilon) {
        assert expected.length == actual.length;

        for (int i = 0; i < expected.length; i++) {
            assertVector4fAlmostEquals(expected[i], actual[i], epsilon);
        }
    }

    private void assertVector4fAlmostEquals(Vector4f a, Vector4f b, float epsilon) {
        assertScalarAlmostEquals(a.x, b.x, epsilon);
        assertScalarAlmostEquals(a.y, b.y, epsilon);
        assertScalarAlmostEquals(a.z, b.z, epsilon);
        assertScalarAlmostEquals(a.w, b.w, epsilon);
    }

    private void assertScalarAlmostEquals(float a, float b, float epsilon) {
        assert Math.abs(a - b) < epsilon;
    }
}