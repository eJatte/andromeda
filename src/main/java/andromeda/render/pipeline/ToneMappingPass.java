package andromeda.render.pipeline;

import andromeda.framebuffer.FrameBuffer;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.shader.Program;

import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;

public class ToneMappingPass {

    private Program program;
    private Geometry quad;

    public void init() {
        this.program = Program.loadShader("shaders/tonemapping.vert", "shaders/tonemapping.frag");
        this.quad = Primitives.quad();
        this.quad.upload();
    }

    public void render(FrameBuffer sourceBuffer, FrameBuffer targetBuffer) {
        targetBuffer.bind();

        program.use();
        program.setInt("renderedTexture", 0);
        sourceBuffer.bindTexture(GL_TEXTURE0);

        quad.draw();
    }
}
