package andromeda.render.pipeline;

import andromeda.framebuffer.FrameBuffer;
import andromeda.framebuffer.GBuffer;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.light.Light;
import andromeda.projection.Camera;
import andromeda.shader.Program;
import andromeda.util.Cascade;

import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.*;

public class LightingPass {
    private Program program;
    private Geometry quad;

    public void init() {
        this.program = Program.loadShader("shaders/lightingPass.vert", "shaders/lightingPass.frag");
        this.quad = Primitives.quad();
        this.quad.upload();
    }

    public void render(Cascade[] cascades, GBuffer gBuffer, FrameBuffer targetBuffer, FrameBuffer depthBuffer, List<Light> lights, Camera camera) {
        program.use();
        program.setCamera(camera);
        program.setLights("lights", lights);

        program.setCascades(cascades);

        program.setInt("gPosition", 0);
        program.setInt("gNormal", 1);
        program.setInt("gAlbedoSpec", 2);
        program.setInt("gSpecular", 3);
        program.setInt("shadow_map", 4);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gBuffer.gPosition);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, gBuffer.gNormal);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, gBuffer.gColor);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, gBuffer.gSpecular);

        depthBuffer.bindTexture(GL_TEXTURE4);


        targetBuffer.bind();
        quad.draw();
    }

}
