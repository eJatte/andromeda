package andromeda.framebuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

public class DepthBufferArray extends FrameBuffer {
    public int fbo, depthTexture, width, height;

    public static FrameBuffer create(int width, int height, int depth) {
        int fbo = glGenFramebuffers();

        int depthTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D_ARRAY, depthTexture);

        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_DEPTH_COMPONENT24, width, height, depth,0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTexture, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        var depthBuffer = new DepthBufferArray();
        depthBuffer.fbo = fbo;
        depthBuffer.depthTexture = depthTexture;
        depthBuffer.width = width;
        depthBuffer.height = height;

        return depthBuffer;
    }

    @Override
    public void bindTexture(int gl_texture_location) {
        glActiveTexture(gl_texture_location);
        glBindTexture(GL_TEXTURE_2D_ARRAY, this.depthTexture);
    }

    @Override
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
        glClear(GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, this.width, this.height);
    }
}
