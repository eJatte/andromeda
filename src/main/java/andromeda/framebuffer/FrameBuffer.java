package andromeda.framebuffer;

import andromeda.window.Screen;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL20C.glDrawBuffers;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

public class FrameBuffer {
    public int fbo, renderTexture, depthBuffer, width, height;

    public static FrameBuffer create(int width, int height) {
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        int renderTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, renderTexture);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height,0, GL_RGB, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        int depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, renderTexture, 0);
        glDrawBuffers(GL_COLOR_ATTACHMENT0);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        var frameBuffer = new FrameBuffer();
        frameBuffer.fbo = fbo;
        frameBuffer.renderTexture = renderTexture;
        frameBuffer.depthBuffer = depthBuffer;
        frameBuffer.width = width;
        frameBuffer.height = height;

        return frameBuffer;
    }

    public void bindTexture(int gl_texture_location) {
        glActiveTexture(gl_texture_location);
        glBindTexture(GL_TEXTURE_2D, this.renderTexture);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
        glClearColor(0.7f, 0.7f, 0.75f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, this.width, this.height);
    }
}
