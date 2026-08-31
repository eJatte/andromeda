package andromeda.ecs.system;

import andromeda.config.DebugSettings;
import andromeda.ecs.Ecs;
import andromeda.ecs.component.SphereCollider;
import andromeda.ecs.component.Transform;
import andromeda.framebuffer.GBuffer;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.projection.Camera;
import andromeda.shader.Program;
import andromeda.util.GraphicsMath;
import andromeda.window.Screen;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Set;

import static andromeda.ecs.component.ComponentType.SPHERE_COLLIDER;
import static andromeda.ecs.component.ComponentType.TRANSFORM;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE1;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

public class DebugRenderSystem extends EcsSystem {
    private Program debugProgram;
    private Geometry circle;
    private CameraSystem cameraSystem;
    private TransformSystem transformSystem;
    private RenderSystem renderSystem;

    private static final Vector3f DEBUG_GREEN = new Vector3f(0.1f,1.0f,0.0f);

    public DebugRenderSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public void init() {
        this.debugProgram = Program.loadShader("shaders/debug_lineloop.vert", "shaders/debug_lineloop.frag");
        this.cameraSystem = ecs.getSystem(CameraSystem.class);
        this.transformSystem = ecs.getSystem(TransformSystem.class);
        this.renderSystem = ecs.getSystem(RenderSystem.class);
        this.circle = Primitives.circle();
        this.circle.upload();
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(TRANSFORM, SPHERE_COLLIDER));
    }

    @Override
    public void update() {
        Camera camera = cameraSystem.getCurrentMainCamera();

        var debugEntities = this.getEntities(TRANSFORM, SPHERE_COLLIDER);

        GBuffer gBuffer = renderSystem.getGBuffer();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        for (int entityId : debugEntities) {
            SphereCollider sphereCollider = ecs.getComponent(SphereCollider.class, entityId);
            if (sphereCollider.debug || DebugSettings.Colliders.enabled) {
                debugDrawCircle(camera, transformSystem.getGlobalPosition(entityId), sphereCollider.radius, gBuffer);
            }
        }
    }

    private void debugDrawCircle(Camera camera, Vector3f position, float radius, GBuffer gBuffer) {
        Matrix4f zAxis = new Matrix4f().translate(position);
        Matrix4f yAxis = zAxis.rotate(GraphicsMath.DEG2RAD*90, new Vector3f(1,0,0), new Matrix4f());
        Matrix4f xAxis = zAxis.rotate(GraphicsMath.DEG2RAD*90, new Vector3f(0,1,0), new Matrix4f());
        this.debugProgram.use();
        this.debugProgram.setCamera(camera);
        this.debugProgram.setVec3("color", DEBUG_GREEN);
        this.debugProgram.setFloat("radius", radius);

        this.debugProgram.setInt("g_position", 0);
        gBuffer.bindTexture("position", GL_TEXTURE0);

        this.debugProgram.setBool("screen_space", true);
        this.debugProgram.setMat4("model",zAxis);
        circle.drawLineLoop();

        this.debugProgram.setBool("screen_space", false);

        this.debugProgram.setMat4("model",zAxis);
        circle.drawLineLoop();
        this.debugProgram.setMat4("model",yAxis);
        circle.drawLineLoop();
        this.debugProgram.setMat4("model",xAxis);
        circle.drawLineLoop();
    }


    @Override
    public SystemType type() {
        return SystemType.DEBUG_RENDER;
    }
}
