package andromeda.window;

import andromeda.event.EventHandler;
import andromeda.input.Input;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private String title;
    private int width, height;

    private long windowId;

    private boolean isFullScreen;
    private int window_x, window_y;
    private int windowWidth, windowHeight;

    public Window(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.isFullScreen = false;
        this.windowWidth = Screen.width;
        this.windowHeight = Screen.height;
    }

    public void create() {
        this.windowId = createWindow(this.title, this.width, this.height);
        setCallbacks(this.windowId);
    }

    public void destroy() {
        glfwFreeCallbacks(this.windowId);
        glfwDestroyWindow(this.windowId);
    }

    public void toggleFullScreen() {
        if (isFullScreen) {
            glfwSetWindowMonitor(windowId, 0, window_x, window_y, windowWidth, windowHeight, GLFW_DONT_CARE);
            isFullScreen = false;
        } else {
            int[] w_x = new int[1];
            int[] w_y = new int[1];
            int[] w_width = new int[1];
            int[] w_height = new int[1];

            glfwGetWindowPos(windowId, w_x, w_y);
            glfwGetWindowSize(windowId, w_width, w_height);

            window_x = w_x[0];
            window_y = w_y[0];
            windowWidth = w_width[0];
            windowHeight = w_height[0];

            long primaryMonitor = glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = glfwGetVideoMode(primaryMonitor);

            glfwSetWindowMonitor(windowId, primaryMonitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
            isFullScreen = true;
        }
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(this.windowId);
    }

    public void hideCursor(boolean toggle) {
        glfwSetInputMode(this.windowId, GLFW_CURSOR, toggle ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }

    private long createWindow(String title, int width, int height) {
        long window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        glfwShowWindow(window);

        return window;
    }

    private void setCallbacks(long windowId) {
        glfwSetKeyCallback(windowId, (w, key, scancode, action, mods) -> {
            if (action == GLFW_RELEASE) {
                Input.get().addKeyboardEvent(key, false);
            } else if (action == GLFW_PRESS) {
                Input.get().addKeyboardEvent(key, true);
            }
        });

        glfwSetMouseButtonCallback(windowId, (w, button, action, mods) -> {
            if (action == GLFW_RELEASE) {
                Input.get().addMouseButtonEvent(button, false);
            } else if (action == GLFW_PRESS) {
                Input.get().addMouseButtonEvent(button, true);
            }
        });

        glfwSetCursorPosCallback(windowId, (w, x, y) -> {
            Input.get().addMousePositionEvent(new Vector2f((float) x, (float) y));
        });

        glfwSetWindowSizeCallback(windowId, (w, x, y) -> {
            Screen.width = x;
            Screen.height = y;
            EventHandler.get().onWindowResize(x, y);
        });
    }

    public long getWindowId() {
        return windowId;
    }
}
