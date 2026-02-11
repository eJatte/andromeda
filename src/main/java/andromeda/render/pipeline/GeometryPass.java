package andromeda.render.pipeline;

import andromeda.framebuffer.GBuffer;
import andromeda.projection.Camera;
import andromeda.scene.RenderTarget;
import andromeda.shader.Program;

import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_BACK;

public class GeometryPass {
    private Program program;

    public void init() {
        this.program = Program.loadShader("shaders/gBuffer.vert", "shaders/gBuffer.frag");
    }

    public void render(Camera camera, List<RenderTarget> renderTargets, GBuffer gBuffer) {
        gBuffer.bind();
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
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
        program.setInt("entityId", renderTarget.getEntityId());

        geometry.draw();
    }
}
