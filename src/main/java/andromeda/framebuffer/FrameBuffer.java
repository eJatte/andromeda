package andromeda.framebuffer;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glDrawBuffers;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

public abstract class FrameBuffer {
    public int fbo;
    public final int width, height;
    private final List<Integer> colorAttachments;
    private final Map<String, Integer> textures;

    public int depthBuffer;

    private Vector4f clearColor;

    public FrameBuffer(int width, int height) {
        this(width, height, new Vector4f(0));
    }

    public FrameBuffer(int width, int height, Vector4f clearColor) {
        this.width = width;
        this.height = height;
        this.clearColor = clearColor;
        this.colorAttachments = new ArrayList<>();
        this.textures = new HashMap<>();
        this.depthBuffer = -1;
    }

    public void create() {
        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        this.createBuffer();

        glDrawBuffers(this.colorAttachments.stream().mapToInt(i -> i).toArray());

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void readPixel(int x, int y, int format, int colorAttachment, float[] target){
        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
        glReadBuffer(colorAttachment);

        glReadPixels(x, y, 1, 1, format, GL_FLOAT, target);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    protected abstract void createBuffer();

    protected int createTexture(String name, int internalFormat, int format, int colorAttachment) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, colorAttachment, GL_TEXTURE_2D, texture, 0);

        colorAttachments.add(colorAttachment);
        textures.put(name, texture);

        return texture;
    }

    protected int createDepthBuffer(int depthComponent) {
        int depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, depthComponent, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        return depthBuffer;
    }

    protected int createDepthTextureArray(int depthComponent, int depth) {
        int depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, depthTexture);
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, depthComponent, width, height, depth, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTexture, 0);

        return depthTexture;
    }

    protected int createDepthTexture(String name, int depthComponent) {
        int depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, depthComponent, width, height,0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glBindTexture(GL_TEXTURE_2D, 0);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTexture, 0);

        textures.put(name, depthTexture);

        return depthTexture;
    }

    protected void noDrawBuffer() {
        glDrawBuffer(GL_NONE);
    }

    protected void noReadBuffer() {
        glReadBuffer(GL_NONE);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
        glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, this.width, this.height);
    }

    public void destroy() {
        glDeleteFramebuffers(this.fbo);
        glDeleteRenderbuffers(this.depthBuffer);
        textures.values().forEach(GL11C::glDeleteTextures);
    }

    public void bindTexture(String texture, int gl_texture_location) {
        glActiveTexture(gl_texture_location);
        glBindTexture(GL_TEXTURE_2D, this.textures.get(texture));
    }
}
