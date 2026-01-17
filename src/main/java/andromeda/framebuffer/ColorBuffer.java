package andromeda.framebuffer;

import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;

public class ColorBuffer extends FrameBuffer {

    public int color;
    private final int internalFormat, format;

    public ColorBuffer(int width, int height, int internalFormat, int format) {
        super(width, height);
        this.internalFormat = internalFormat;
        this.format = format;
    }

    @Override
    protected void createBuffer() {
        color = createTexture("color", internalFormat, format, GL_COLOR_ATTACHMENT0);
    }
}
