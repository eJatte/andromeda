package andromeda.render.pipeline;

import andromeda.config.GraphicsSettings;
import andromeda.framebuffer.FrameBuffer;
import andromeda.framebuffer.GBuffer;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.projection.Camera;
import andromeda.shader.Program;
import andromeda.util.GraphicsMath;
import org.joml.Vector3f;

import java.util.Random;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL30C.GL_RGBA16F;

public class AmbientOcclusionPass {
    private Program program, blurProgram;
    private Geometry quad;
    private Vector3f[] hemisphereSamples;
    private int n_samples = GraphicsSettings.AmbientOcclusion.n_samples;
    private int noiseTexture;

    public AmbientOcclusionPass() {
    }

    public void init() {
        this.program = Program.loadShader("shaders/ambientOcclusionPass.vert", "shaders/ambientOcclusionPass.frag");
        this.blurProgram = Program.loadShader("shaders/ambientOcclusionPass.vert", "shaders/blur.frag");
        this.quad = Primitives.quad();
        this.quad.upload();

        hemisphereSamples = createSamples(GraphicsSettings.AmbientOcclusion.n_samples);
        noiseTexture = createTexture();
    }

    private Vector3f[] createSamples(int n) {
        Vector3f[] samples = new Vector3f[n];

        Random random = new Random();

        for (int i = 0; i < n; i++) {
            float x = random.nextFloat(-1.0f, 1.0f);
            float y = random.nextFloat(-1.0f, 1.0f);
            float z = random.nextFloat(0.0f, 1.0f);

            float scale = (float) i / (float) n;
            scale = GraphicsMath.lerp(0.1f, 1.0f, scale * scale);

            samples[i] = new Vector3f(x, y, z).normalize().mul(scale);
        }

        return samples;
    }

    private int createTexture() {
        float[] buffer = new float[16 * 3];
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            buffer[i * 3 + 0] = random.nextFloat(-1, 1);
            buffer[i * 3 + 1] = random.nextFloat(-1, 1);
            buffer[i * 3 + 2] = 0;
        }

        int noise = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, noise);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, 4, 4, 0, GL_RGB, GL_FLOAT, buffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glBindTexture(GL_TEXTURE_2D, 0);

        return noise;
    }

    public void render(GBuffer gBuffer, FrameBuffer targetBuffer, FrameBuffer blurTarget, Camera camera) {
        if (n_samples != GraphicsSettings.AmbientOcclusion.n_samples) {
            n_samples = GraphicsSettings.AmbientOcclusion.n_samples;
            hemisphereSamples = createSamples(n_samples);
        }

        program.use();
        program.setCamera(camera);

        program.setInt("gPosition", 0);
        program.setInt("gNormal", 1);
        program.setInt("gAlbedo", 2);
        program.setInt("noise", 3);
        program.setVec3("samples", hemisphereSamples);

        program.setFloat("radius", GraphicsSettings.AmbientOcclusion.radius);
        program.setInt("n_samples", GraphicsSettings.AmbientOcclusion.n_samples);
        program.setFloat("power", GraphicsSettings.AmbientOcclusion.power);
        program.setFloat("bias", GraphicsSettings.AmbientOcclusion.bias);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gBuffer.gPosition);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, gBuffer.gNormal);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, gBuffer.gColor);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, noiseTexture);

        targetBuffer.bind();
        quad.draw();

        blurProgram.use();
        blurProgram.setInt("inputTexture", 0);

        targetBuffer.bindTexture("color", GL_TEXTURE0);

        blurTarget.bind();
        quad.draw();
    }

}
