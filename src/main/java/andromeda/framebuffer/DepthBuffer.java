package andromeda.framebuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glDrawBuffers;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

public class DepthBuffer extends FrameBuffer {
    private int depthTexture;

    public DepthBuffer(int width, int height) {
        super(width, height);
    }

    @Override
    protected void createBuffer() {
        depthTexture = createDepthTexture("depth", GL_DEPTH_COMPONENT24);
        noDrawBuffer();
        noReadBuffer();
    }
}
