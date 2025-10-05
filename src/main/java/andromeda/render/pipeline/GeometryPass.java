package andromeda.render.pipeline;

import andromeda.framebuffer.GBuffer;
import andromeda.projection.Camera;
import andromeda.scene.RenderTarget;
import andromeda.shader.Program;

import java.util.List;

public class GeometryPass {
    private Program program;

    public void init() {
        this.program = Program.loadShader("shaders/gBuffer.vert", "shaders/gBuffer.frag");
    }

    public void render(Camera camera, List<RenderTarget> renderTargets, GBuffer gBuffer) {
        gBuffer.bind();
        renderTargets.forEach(r -> this.render(camera, r));
    }

    private void render(Camera camera, RenderTarget renderTarget) {
        var mesh = renderTarget.getMesh();
        var model = renderTarget.getTransform();
        var geometry = mesh.getGeometry();

        program.use();
        program.setCamera(camera);
        program.setMaterial("material", mesh.getMaterial());
        program.setMat4("model", model);

        geometry.draw();
    }
}
