package andromeda.render.pipeline;

import andromeda.config.GraphicsSettings;
import andromeda.framebuffer.FrameBuffer;
import andromeda.framebuffer.GBuffer;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.projection.Camera;
import andromeda.shader.Program;

import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE1;

public class PostProcessingPass {

    private Program program;
    private Geometry quad;

    public void init() {
        this.program = Program.loadShader("shaders/post_processing.vert", "shaders/post_processing.frag");
        this.quad = Primitives.quad();
        this.quad.upload();
    }

    public void render(GBuffer gBuffer, FrameBuffer sourceBuffer, FrameBuffer targetBuffer, Camera camera) {
        targetBuffer.bind();

        program.use();

        program.setCamera(camera);

        program.setVec3("fogColor", GraphicsSettings.Fog.color);
        program.setFloat("fogDistance", GraphicsSettings.Fog.depth);
        program.setFloat("fogPower", GraphicsSettings.Fog.power);

        program.setInt("renderedTexture", 0);
        sourceBuffer.bindTexture("color", GL_TEXTURE0);
        program.setInt("g_position", 1);
        gBuffer.bindTexture("position", GL_TEXTURE1);

        quad.draw();
    }
}
