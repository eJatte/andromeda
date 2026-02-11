package andromeda.render.pipeline;

import andromeda.framebuffer.FrameBuffer;
import andromeda.light.DirectionalLight;
import andromeda.projection.Camera;
import andromeda.projection.OrthographicCamera;
import andromeda.scene.RenderTarget;
import andromeda.shader.Program;
import andromeda.util.Cascade;
import andromeda.util.GraphicsMath;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class ShadowPass {
    private static final int CASCADE_LEVELS = 4;
    private static final int SHADOW_MAP_SIZE = 2048;
    private Program program;

    public void init() {
        this.program = Program.loadShader("shaders/shadow_cascade.vert", "shaders/shadow_cascade.frag", "shaders/shadow_cascade.geom");
    }

    public Cascade[] getCascades(Camera camera, DirectionalLight directionalLight) {
        var levels = new float[]{0.1f, 0.3f, 0.6f, 1.0f};

        if (CASCADE_LEVELS != levels.length) {
            throw new IllegalArgumentException("Wrong number of levels in levels array, expected %d".formatted(CASCADE_LEVELS));
        }

        var near = camera.getNear();
        var far = camera.getFar();
        var view = camera.getView();

        var shadowDir = directionalLight.position;

        Cascade[] cascades = new Cascade[CASCADE_LEVELS];

        float prev_far = near;
        for (int i = 0; i < CASCADE_LEVELS; i++) {
            float level_i_far = (far * levels[i]);
            Matrix4f level_i_frustum = camera.getProjection(prev_far, level_i_far);
            OrthographicCamera level_i_ortho = directionalOrtho(shadowDir, level_i_frustum, view);

            cascades[i] = new Cascade(level_i_ortho.getProjectionView(), levels[i], level_i_ortho.getLeft());

            prev_far = level_i_far;
        }

        return cascades;
    }

    public void render(List<RenderTarget> renderTargets, Camera camera, DirectionalLight directionalLight, FrameBuffer depthBuffer) {
        Cascade[] cascades = getCascades(camera, directionalLight);
        this.renderShadow(renderTargets, cascades, depthBuffer, this.program);
    }

    private void renderShadow(List<RenderTarget> renderTargets, Cascade[] cascades, FrameBuffer targetBuffer, Program program) {
        targetBuffer.bind();

        for (var target : renderTargets) {
            if (!target.getMesh().getMaterial().unlit) {
                this.renderShadow(target, cascades, program);
            }
        }
    }

    private void renderShadow(RenderTarget renderTarget, Cascade[] cascades, Program program) {
        var mesh = renderTarget.getMesh();
        var model = renderTarget.getTransform();
        var geometry = mesh.getGeometry();

        program.use();
        program.setCascades(cascades);
        program.setMat4("model", model);

        geometry.draw();
    }

    private OrthographicCamera directionalOrtho(Vector3f dir, Matrix4f projection, Matrix4f view) {
        var boundingSphere = GraphicsMath.getBoundingSphere(projection, view);

        var orthoCamera = new OrthographicCamera(boundingSphere.radius, boundingSphere.radius);
        orthoCamera.setPosition(dir.mul(100, new Vector3f()).add(new Vector3f(0)));
        orthoCamera.lookAtPoint(new Vector3f(0));
        orthoCamera.setFar(200);

        var orthoView = orthoCamera.getView();
        var inverseOrthoView = orthoView.invertAffine(new Matrix4f());

        var radius = boundingSphere.radius;
        var min_size = radius / SHADOW_MAP_SIZE * 2;

        var shadowSpacePos = new Vector4f(boundingSphere.position, 1).mul(orthoView);
        shadowSpacePos = new Vector4f(shadowSpacePos.x - shadowSpacePos.x % min_size, shadowSpacePos.y - shadowSpacePos.y % min_size, shadowSpacePos.z, shadowSpacePos.w);
        var snappedWorldSpacePos = shadowSpacePos.mul(inverseOrthoView, new Vector4f()).xyz(new Vector3f());

        orthoCamera.setPosition(dir.mul(100, new Vector3f()).add(snappedWorldSpacePos));
        orthoCamera.lookAtPoint(snappedWorldSpacePos);

        return orthoCamera;
    }

}
