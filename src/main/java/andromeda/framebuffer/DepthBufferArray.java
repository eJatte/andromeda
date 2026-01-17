package andromeda.framebuffer;

import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL30C.*;

public class DepthBufferArray extends FrameBuffer {

    private final int depth;
    private int depthTexture;

    public DepthBufferArray(int width, int height, int depth) {
        super(width, height);
        this.depth = depth;
    }

    @Override
    protected void createBuffer() {
        depthTexture = createDepthTextureArray(GL_DEPTH_COMPONENT24, depth);
        noDrawBuffer();
        noReadBuffer();
    }

    @Override
    public void bindTexture(String s, int gl_texture_location) {
        glActiveTexture(gl_texture_location);
        glBindTexture(GL_TEXTURE_2D_ARRAY, this.depthTexture);
    }
}
