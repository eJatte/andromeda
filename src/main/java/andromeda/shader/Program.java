package andromeda.shader;

import andromeda.light.Light;
import andromeda.material.Material;
import andromeda.resources.MaterialRepresentation;
import andromeda.texture.Texture;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.*;

public class Program {

    int program;

    public Program() {
    }

    public void destroy() {
        glDeleteProgram(this.program);
    }

    public void link(Shader shader) {
        this.program = glCreateProgram();
        glAttachShader(this.program, shader.getVertexShader());
        glAttachShader(this.program, shader.getFragmentShader());
        glLinkProgram(this.program);

        int status = glGetProgrami(this.program, GL_LINK_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException("Failed to link program: " + glGetProgramInfoLog(this.program));
        }
    }

    public void use() {
        glUseProgram(this.program);
    }

    public void setInt(String name, int i) {
        glUniform1i(getUniformLocation(name), i);
    }

    public void setBool(String name, boolean b) {
        glUniform1i(getUniformLocation(name), b ? 1 : 0);
    }

    public void setFloat(String name, float f) {
        glUniform1f(getUniformLocation(name), f);
    }

    public void setVec4(String name, Vector4f v) {
        glUniform4f(getUniformLocation(name), v.x, v.y, v.z, v.y);
    }

    public void setVec3(String name, Vector3f v) {
        glUniform3f(getUniformLocation(name), v.x, v.y, v.z);
    }

    public void setMat4(String name, Matrix4f mat4){
        var buffer = BufferUtils.createFloatBuffer(4*4);
        glUniformMatrix4fv(getUniformLocation(name), false, mat4.get(buffer));
    }

    public void setMaterial(String name, Material material) {
        this.setVec3("%s.ambient".formatted(name), material.ambient);
        this.setVec3("%s.diffuse".formatted(name), material.diffuse);
        this.setVec3("%s.specular".formatted(name), material.specular);
        this.setFloat("%s.shininess".formatted(name), material.shininess);

        if (material.diffuse_texture != null) {
            material.diffuse_texture.bind(GL_TEXTURE0);
            this.setInt("diffuse_texture", 0);
        }
        else {
            this.setBool("has_diffuse_texture", false);
        }

        if (material.diffuse_texture != null) {
            material.normal_texture.bind(GL_TEXTURE1);
            this.setInt("normal_texture", 1);
        }
        else {
            this.setBool("has_normal_texture", false);
        }
    }

    public void setLight(String name, Light light) {
        this.setVec3("%s.position".formatted(name), light.position);
        this.setVec3("%s.ambient".formatted(name), light.ambient);
        this.setVec3("%s.diffuse".formatted(name), light.diffuse);
        this.setVec3("%s.specular".formatted(name), light.specular);

    }

    public void setLight(String name, int index, Light light) {
        this.setLight("%s[%d]".formatted(name, index), light);
    }

    public void setLights(String name, List<Light> lights) {
        int size = lights.size();
        for (int i = 0; i < size; i++) {
            var light = lights.get(i);
            setLight(name, i, light);
        }

        setInt("lightCount", size);
    }

    private int getUniformLocation(String name) {
        return glGetUniformLocation(this.program, name);
    }

    private static Map<String, Program> programs = new HashMap<>();

    public static Program loadShader(String vert_path, String frag_path) {
        var key = vert_path + "-" + frag_path;
        if (!programs.containsKey(key)) {
            var shader = new Shader(vert_path, frag_path);
            shader.compile();

            var program = new Program();
            program.link(shader);
            shader.destroy();

            programs.put(key, program);
        }
        return programs.get(key);
    }
}
