package andromeda.framebuffer;

import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL30C.*;

public class GBuffer extends FrameBuffer {
    public int gPosition, gNormal, gColor, gSpecular;

    public GBuffer(int width, int height) {
        super(width, height, new Vector4f(0, 0,0, -1));
    }

    @Override
    protected void createBuffer() {
        this.gPosition = createTexture("position", GL_RGBA32F, GL_RGBA, GL_COLOR_ATTACHMENT0);
        this.gNormal = createTexture("normal", GL_RGBA16F, GL_RGBA, GL_COLOR_ATTACHMENT1);
        this.gColor = createTexture("color", GL_RGBA, GL_RGBA, GL_COLOR_ATTACHMENT2);
        this.gSpecular = createTexture("specular", GL_RGBA, GL_RGBA, GL_COLOR_ATTACHMENT3);
        this.depthBuffer = createDepthBuffer(GL_DEPTH_COMPONENT);
    }
}
