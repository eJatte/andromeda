package andromeda;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.CameraComponent;
import andromeda.ecs.component.FpsControl;
import andromeda.ecs.component.Perspective;
import andromeda.ecs.component.Transform;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.resources.SceneLoader;
import andromeda.window.Screen;
import andromeda.window.Window;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

public class Controller {
    private Ecs ecs;
    private Window window;
    protected ImGuiImplGlfw imGuiGlfw;
    protected ImGuiImplGl3 imGuiGl3;

    public static boolean MOUSE_ENABLED = true;

    public Controller() {
    }

    public void init(String scene_path, Window window, ImGuiImplGlfw imGuiGlfw, ImGuiImplGl3 imGuiGl3) {
        this.window = window;
        this.imGuiGl3 = imGuiGl3;
        this.imGuiGlfw = imGuiGlfw;

        this.ecs = new Ecs();
        ecs.init();

        int cameraEntity = ecs.createEntity();
        CameraComponent cameraComponent = ecs.addComponent(CameraComponent.class, cameraEntity);
        cameraComponent.mainCamera = true;
        Perspective perspective = ecs.addComponent(Perspective.class, cameraEntity);
        perspective.aspectRatio = Screen.width / (float) Screen.height;
        FpsControl fpsControl = ecs.addComponent(FpsControl.class, cameraEntity);
        Transform transform = ecs.getComponent(Transform.class, cameraEntity);
        transform.setName("Main Camera");

        fpsControl.targetPosition = new Vector3f(10, 10, 10);
        fpsControl.targetPitch = -20;
        fpsControl.targetYaw = 45;

        SceneLoader.loadSceneEcs(scene_path, ecs);
    }

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

            ecs.update();

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
        if (Input.get().keyUp(KeyCode.KEY_F1)) {
            MOUSE_ENABLED = !MOUSE_ENABLED;
            this.window.hideCursor(!MOUSE_ENABLED);
            Input.get().setMouseEnabled(!MOUSE_ENABLED);
        }

        if(Input.get().keyUp(KeyCode.KEY_F11)) {
            this.window.toggleFullScreen();
        }

    }
}
