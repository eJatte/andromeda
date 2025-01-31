package andromeda.texture;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL46C.*;

public class TextureData {
    public ByteBuffer buffer;
    public int width, height, n_channels;

    public int format;

    public TextureData load(String path) {
        MemoryStack stack = MemoryStack.stackPush();
        IntBuffer w = stack.mallocInt(1);
        IntBuffer h = stack.mallocInt(1);
        IntBuffer channels = stack.mallocInt(1);

        var file = new File(path);

        STBImage.stbi_set_flip_vertically_on_load(true);
        this.buffer = STBImage.stbi_load(file.getAbsolutePath(), w, h, channels, 0);

        if (this.buffer == null) {
            throw new IllegalArgumentException("Can't load file " + path + " " + STBImage.stbi_failure_reason());
        }

        this.width = w.get();
        this.height = h.get();
        this.n_channels = channels.get();
        this.format = glFormat();

        stack.close();

        return this;
    }

    private int glFormat() {
        return switch (this.n_channels) {
            case 1 -> GL_RED;
            case 2 -> GL_RG;
            case 3 -> GL_RGB;
            case 4 -> GL_RGBA;
            default -> throw new IllegalStateException("Could not determine texture format!");
        };
    }

    public void destroy() {
        STBImage.stbi_image_free(this.buffer);
    }

}
