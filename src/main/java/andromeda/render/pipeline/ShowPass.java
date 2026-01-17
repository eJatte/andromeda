package andromeda.render.pipeline;

import andromeda.ecs.Ecs;
import andromeda.ecs.system.PropertiesSystem;
import andromeda.framebuffer.FrameBuffer;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.shader.Program;
import andromeda.window.Screen;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

public class ShowPass {

    private Program program;
    private Geometry quad;
    private Ecs ecs;

    public ShowPass(Ecs ecs) {
        this.ecs = ecs;
    }

    public void init() {
        this.program = Program.loadShader("shaders/framebuffer.vert", "shaders/framebuffer.frag");
        this.quad = Primitives.quad();
        this.quad.upload();
    }

    public void render(int sourceTexture) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        glViewport(0, 0, Screen.width, Screen.height);


        program.use();
        program.setInt("renderedTexture", 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture);

        quad.draw();
    }
}
