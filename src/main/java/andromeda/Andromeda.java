package andromeda;

import andromeda.window.Screen;
import andromeda.window.Window;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;

public class Andromeda {

    protected ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    protected ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public void run(String scenePath) {
        initGlfw();

        var window = new Window("Andromeda", Screen.width, Screen.height);
        window.create();

        initGl();

        initImGUI(window);

        var controller = new Controller();

        controller.init(scenePath, window, imGuiGlfw, imGuiGl3);

        controller.loop();

        destroyImGUI();
        window.destroy();
        destroyGlfw();
    }

    private void initGlfw() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
    }

    private void initGl() {
        GL.createCapabilities();
    }

    private void initImGUI(Window window) {
        ImGui.createContext();
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable);
        this.imGuiGlfw.init(window.getWindowId(), true);
        this.imGuiGl3.init();
    }

    private void destroyImGUI() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }

    private void destroyGlfw() {
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please specify a scene path");

        } else {
            new Andromeda().run(args[0]);
        }
    }
}