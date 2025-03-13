package andromeda;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.CameraComponent;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.resources.SceneLoader;
import andromeda.window.Window;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;

public class Controller {
    private EcsCoordinator ecsCoordinator;
    private Window window;
    protected ImGuiImplGlfw imGuiGlfw;
    protected ImGuiImplGl3 imGuiGl3;

    int cameraEntity;

    public Controller() {
    }

    public void init(String scene_path, Window window, ImGuiImplGlfw imGuiGlfw, ImGuiImplGl3 imGuiGl3) {
        this.window = window;
        this.imGuiGl3 = imGuiGl3;
        this.imGuiGlfw = imGuiGlfw;

        this.ecsCoordinator = new EcsCoordinator();
        ecsCoordinator.init();

        cameraEntity = ecsCoordinator.createEntity();
        CameraComponent cameraComponent = ecsCoordinator.addComponent(CameraComponent.class, cameraEntity);
        cameraComponent.setMainCamera(true);

        SceneLoader.loadSceneEcs(scene_path, ecsCoordinator);
    }

    float[] color = new float[4];
    boolean first_time = true;
    public void loop() {
        long start, end;
        float frame_time = 0;

        System.currentTimeMillis();

        while (!window.shouldClose()) {
            start = System.nanoTime();

            Input.get().update();
            updateMouseEnabled();

            imGuiGl3.newFrame();
            imGuiGlfw.newFrame();
            ImGui.newFrame();

            ecsCoordinator.update();

            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            glfwSwapBuffers(window.getWindowId());
            glfwPollEvents();

            end = System.nanoTime();
            frame_time = end - start;
            DeltaTime.deltaTime = frame_time / 1e9f;
        }
    }

    private void updateMouseEnabled() {
        if (Input.get().keyUp(KeyCode.KEY_1)) {
            this.window.hideCursor(true);
            Input.get().setMouseEnabled(true);
        }

        if (Input.get().keyUp(KeyCode.KEY_ESCAPE)) {
            this.window.hideCursor(false);
            Input.get().setMouseEnabled(false);
        }


    }
}
