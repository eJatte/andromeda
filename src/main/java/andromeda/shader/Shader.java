package andromeda.shader;

import andromeda.file.FileUtil;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;

public class Shader {
    private final String vertexShaderPath, fragmentShaderPath, geometryShaderPath;
    private int vertexShader = -1, fragmentShader = -1, geometryShader = -1;

    public Shader(String vertexShaderPath, String fragmentShaderPath) {
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
        this.geometryShaderPath = null;
    }

    public Shader(String vertexShaderPath, String fragmentShaderPath, String geometryShaderPath) {
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
        this.geometryShaderPath = geometryShaderPath;
    }

    public void destroy() {
        glDeleteShader(this.vertexShader);
        glDeleteShader(this.fragmentShader);
        glDeleteShader(this.geometryShader);
    }

    public void compile() {
        var vsShaderSource = FileUtil.readFile(this.vertexShaderPath);
        var fsShaderSource = FileUtil.readFile(this.fragmentShaderPath);


        this.vertexShader = createShader(vsShaderSource, GL_VERTEX_SHADER);
        this.fragmentShader = createShader(fsShaderSource, GL_FRAGMENT_SHADER);

        if(this.geometryShaderPath != null) {
            var gsShaderSource = FileUtil.readFile(this.geometryShaderPath);
            this.geometryShader = createShader(gsShaderSource, GL_GEOMETRY_SHADER);
        }
    }

    public int getVertexShader() {
        return vertexShader;
    }

    public int getFragmentShader() {
        return fragmentShader;
    }

    public int getGeometryShader() {
        return geometryShader;
    }

    private int createShader(CharSequence source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException("Failed to create shader: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }
}
