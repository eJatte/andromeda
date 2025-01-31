package andromeda.texture;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46C.*;

public class Texture {

    private int width, height;
    private int n_channels;
    private int texture_id;

    public Texture(int width, int height, int n_channels, int texture_id) {
        this.width = width;
        this.height = height;
        this.n_channels = n_channels;
        this.texture_id = texture_id;
    }

    public void bind(int gl_texture_location) {
        glActiveTexture(gl_texture_location);
        glBindTexture(GL_TEXTURE_2D, this.texture_id);
    }

    private static Map<String, Texture> textures = new HashMap<>();

    public static Texture loadTexture(String texturePath) {
        return loadTexture(texturePath, true);
    }

    public static Texture loadNormalTexture(String texturePath) {
        return loadTexture(texturePath, false);
    }

    private static Texture loadTexture(String texturePath, boolean srgb) {
        if (!textures.containsKey(texturePath)) {

            var textureData = new TextureData().load(texturePath);

            int id = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, id);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            var internalformat = srgb ? GL_SRGB : GL_RGB;
            if (textureData.n_channels == 4)
                internalformat = srgb ? GL_SRGB_ALPHA : GL_RGBA;

            glTexImage2D(GL_TEXTURE_2D, 0, internalformat, textureData.width, textureData.height, 0, textureData.format, GL_UNSIGNED_BYTE, textureData.buffer);

            glGenerateMipmap(GL_TEXTURE_2D);

            textureData.destroy();

            var texture = new Texture(textureData.width, textureData.height, textureData.n_channels, id);
            textures.put(texturePath, texture);
        }

        return textures.get(texturePath);
    }
}
