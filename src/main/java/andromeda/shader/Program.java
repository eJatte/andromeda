package andromeda.shader;

import andromeda.light.Light;
import andromeda.light.LightType;
import andromeda.light.PointLight;
import andromeda.material.Material;
import andromeda.projection.Camera;
import andromeda.util.Cascade;
import andromeda.window.Screen;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

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
        if(shader.getGeometryShader() != -1) {
            glAttachShader(this.program, shader.getGeometryShader());
        }
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

    public void setFloat(String name, int index, float f) {
        this.setFloat("%s[%d]".formatted(name, index), f);
    }

    public void setFloat(String name, List<Float> fs) {
        int size = fs.size();
        for (int i = 0; i < size; i++) {
            setFloat(name, i, fs.get(i));
        }
    }

    public void setVec4(String name, Vector4f v) {
        glUniform4f(getUniformLocation(name), v.x, v.y, v.z, v.y);
    }

    public void setVec3(String name, Vector3f v) {
        glUniform3f(getUniformLocation(name), v.x, v.y, v.z);
    }

    public void setVec2(String name, Vector2f v) {
        glUniform2f(getUniformLocation(name), v.x, v.y);
    }

    public void setMat4(String name, Matrix4f mat4) {
        glUniformMatrix4fv(getUniformLocation(name), false, mat4.get(new float[4*4]));
    }

    public void setMat4(String name, int index, Matrix4f mat4) {
        this.setMat4("%s[%d]".formatted(name, index), mat4);
    }

    public void setMat4(String name, Matrix4f[] matrix4fs) {
        int size = matrix4fs.length;
        for (int i = 0; i < size; i++) {
            setMat4(name, i, matrix4fs[i]);
        }
    }

    public void setCascades(Cascade[] cascades) {
        int size = cascades.length;
        for (int i = 0; i < size; i++) {
            this.setMat4("%s[%d]".formatted("lightSpaceMatrices", i), cascades[i].lightSpaceProjection);
            this.setFloat("%s[%d]".formatted("frustumSizes", i), cascades[i].size);
            this.setFloat("%s[%d]".formatted("levelDistances", i), cascades[i].distance);
        }
        this.setInt("cascadeLevels", size);
    }

    public void setCamera(Camera camera) {
        this.setMat4("projection", camera.getProjectionWH(Screen.VIEWPORT_WIDTH, Screen.VIEWPORT_HEIGHT));
        this.setMat4("view", camera.getView());
        this.setVec3("eyePos", camera.getPosition());
    }

    public void setMaterial(String name, Material material) {
        this.setVec3("%s.ambient".formatted(name), material.ambient);
        this.setVec3("%s.diffuse".formatted(name), material.diffuse);
        this.setVec3("%s.specular".formatted(name), material.specular);
        this.setFloat("%s.shininess".formatted(name), material.shininess);
        this.setVec2("%s.texture_scale".formatted(name), material.texture_scale);

        if (material.diffuse_texture != null) {
            material.diffuse_texture.bind(GL_TEXTURE0);
            this.setInt("diffuse_texture", 0);
            this.setBool("has_diffuse_texture", true);
        } else {
            this.setBool("has_diffuse_texture", false);
        }

        if (material.normal_texture != null) {
            material.normal_texture.bind(GL_TEXTURE1);
            this.setInt("normal_texture", 1);
            this.setBool("has_normal_texture", true);
        } else {
            this.setBool("has_normal_texture", false);
        }

        if (material.roughness_texture != null) {
            material.roughness_texture.bind(GL_TEXTURE2);
            this.setInt("roughness_texture", 0);
            this.setBool("has_roughness_texture", true);
        } else {
            this.setBool("has_roughness_texture", false);
        }
    }

    public void setLight(String name, Light light) {
        this.setVec3("%s.position".formatted(name), light.position);
        this.setVec3("%s.diffuse".formatted(name), light.diffuse);
        this.setVec3("%s.specular".formatted(name), light.specular);
        this.setBool("%s.castShadows".formatted(name), light.castShadows);

        if (light.type() == LightType.DIRECTIONAL) {
            this.setInt("%s.type".formatted(name), 0);
        }
        if (light.type() == LightType.POINT) {
            this.setInt("%s.type".formatted(name), 1);
            this.setFloat("%s.radius".formatted(name), ((PointLight) light).radius);
        }
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
        return loadShader(vert_path, frag_path, null);
    }

    public static Program loadShader(String vert_path, String frag_path, String geom_path) {
        var key = vert_path + "-" + frag_path;
        if (!programs.containsKey(key)) {
            var shader = new Shader(vert_path, frag_path, geom_path);
            shader.compile();

            var program = new Program();
            program.link(shader);
            shader.destroy();

            programs.put(key, program);
        }
        return programs.get(key);
    }
}
