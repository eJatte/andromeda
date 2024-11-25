package andromeda;

import andromeda.entity.Entity;
import andromeda.entity.RotateUpdatable;
import andromeda.entity.Updatable;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.light.Light;
import andromeda.material.Material;
import andromeda.projection.Camera;
import andromeda.resources.MaterialRepresentation;
import andromeda.shader.Program;
import andromeda.shader.Shader;
import andromeda.texture.Texture;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HelloWorld {

    // The window handle
    private long window;

    private int width = 1280, height = 720;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);

        // Create the window
        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_RELEASE) {
                Input.get().addKeyboardEvent(key, false);
            } else if (action == GLFW_PRESS) {
                Input.get().addKeyboardEvent(key, true);
            }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (action == GLFW_RELEASE) {
                Input.get().addMouseButtonEvent(button, false);
            } else if (action == GLFW_PRESS) {
                Input.get().addMouseButtonEvent(button, true);
            }
        });

        glfwSetCursorPosCallback(window, (window, x, y) -> {
            Input.get().addMousePositionEvent(new Vector2f((float) x, (float) y));
        });

        glfwSetWindowSizeCallback(window, (window, x, y) -> {
            this.width = x;
            this.height = y;
            glViewport(0, 0, this.width, this.height);
        });


        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);

        List<Entity> entities = new ArrayList<>();

        var geometry = Primitives.cube();
        geometry.upload();
        var material = Material.loadMaterial("materials/brick_material.json");
        var cube = new Entity(material, geometry, new RotateUpdatable());

        entities.add(cube);

        var plane_geo = Primitives.grid(10, 10, 1);
        plane_geo.upload();
        var plane = new Entity(material, plane_geo, (e) -> {
        });
        plane.transform().translate(-5, -1.0f, -5);
        entities.add(plane);

        var light_shader = new Shader("shaders/phong.vert", "shaders/light_shader.frag");
        light_shader.compile();

        var light_program = new Program();
        light_program.link(light_shader);

        var camera = new Camera();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        var light = new Light(new Vector3f(0, 0, 4), new Vector3f(1.0f));
        var light2 = new Light(new Vector3f(-4, 4, -4), new Vector3f(0.6f, 0.1f, 0.1f));
        var light3 = new Light(new Vector3f(4, 4, -4), new Vector3f(0.1f, 0.6f, 0.1f));
        var light4 = new Light(new Vector3f(0, 4, -4), new Vector3f(0.1f, 0.1f, 0.6f));

        var lights = List.of(light, light2, light3, light4);

        boolean wireframe = false;

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            Input.get().update();

            if (Input.get().keyUp(KeyCode.KEY_R)) {
                wireframe = !wireframe;
                glPolygonMode(GL_FRONT_AND_BACK, wireframe ? GL_LINE : GL_FILL);
            }

            if (Input.get().keyUp(KeyCode.MOUSE_BUTTON_1)) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                camera.setMouseEnabled(true);
            }

            if (Input.get().keyUp(KeyCode.KEY_ESCAPE)) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                camera.setMouseEnabled(false);
            }

            camera.update();

            updateEntities(entities);
            renderEntities(entities, camera, lights);

            renderLight(light, geometry, light_program, camera, width, height);
            renderLight(light2, geometry, light_program, camera, width, height);
            renderLight(light3, geometry, light_program, camera, width, height);
            renderLight(light4, geometry, light_program, camera, width, height);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void updateEntities(List<Entity> entities) {
        entities.forEach(this::updateEntity);
    }

    private void updateEntity(Entity entity) {
        entity.updatable().ifPresent(u -> u.update(entity));
        entity.children().forEach(this::updateEntity);
    }

    private void renderEntities(List<Entity> entities, Camera camera, List<Light> lights) {
        entities.forEach(entity -> renderEntity(entity, camera, lights, new Matrix4f()));
    }

    private void renderEntity(Entity entity, Camera camera, List<Light> lights, Matrix4f parent_transform) {
        var transform = parent_transform.mul(entity.transform(), new Matrix4f());
        if (entity.geometry().isPresent() && entity.material().isPresent()) {
            var geometry = entity.geometry().get();
            var material = entity.material().get();
            var program = material.program;

            program.use();
            program.setMat4("projection", camera.getProjection(width, height));
            program.setMat4("view", camera.getView());
            program.setVec3("eyePos", camera.getPosition());
            program.setLights("lights", lights);

            program.setMaterial("material", material);
            program.setMat4("model", transform);
            geometry.render(program);
        }

        for (var child : entity.children()) {
            renderEntity(child, camera, lights, transform);
        }
    }

    private static void renderLight(Light light, Geometry geometry, Program program, Camera camera, int width, int height) {
        var light_model = new Matrix4f().translate(light.position).scale(0.2f);
        // render light
        program.use();
        program.setMat4("projection", camera.getProjection(width, height));
        program.setMat4("view", camera.getView());
        program.setMat4("model", light_model);
        program.setVec3("color", light.diffuse);
        geometry.render(program);
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }
}