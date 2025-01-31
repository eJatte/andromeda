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
import andromeda.resources.ModelLoader;
import andromeda.resources.SceneLoader;
import andromeda.scene.Scene;
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

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        var scene_path = "scenes/basic_scene.json";

        var scene = SceneLoader.loadScene(scene_path);
        var camera = new Camera();

        boolean wireframe = false;

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            Input.get().update();

            camera.setWidth(width);
            camera.setHeight(height);

            if (Input.get().keyUp(KeyCode.KEY_U)) {
                wireframe = !wireframe;
                glPolygonMode(GL_FRONT_AND_BACK, wireframe ? GL_LINE : GL_FILL);
            }

            if (Input.get().keyUp(KeyCode.KEY_R)) {
                scene = SceneLoader.loadScene(scene_path);
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

            scene.update();
            scene.render(camera);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }
}