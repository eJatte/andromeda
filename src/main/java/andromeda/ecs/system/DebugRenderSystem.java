package andromeda.ecs.system;

import andromeda.config.DebugSettings;
import andromeda.ecs.Ecs;
import andromeda.ecs.component.RigidBody;
import andromeda.ecs.component.SphereCollider;
import andromeda.framebuffer.GBuffer;
import andromeda.geometry.Geometry;
import andromeda.geometry.Primitives;
import andromeda.projection.Camera;
import andromeda.shader.Program;
import andromeda.util.Colors;
import andromeda.util.GraphicsMath;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Set;

import static andromeda.ecs.component.ComponentType.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

public class DebugRenderSystem extends EcsSystem {
    private Program debugProgram;
    private Geometry circle, line, cone;
    private CameraSystem cameraSystem;
    private TransformSystem transformSystem;
    private RenderSystem renderSystem;

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
        this.line = Primitives.line();
        this.line.upload();
        this.cone = Primitives.cone();
        this.cone.upload();
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(TRANSFORM, SPHERE_COLLIDER), Signature.of(TRANSFORM, RIGID_BODY));
    }

    @Override
    public void update() {
        Camera camera = cameraSystem.getCurrentMainCamera();
        GBuffer gBuffer = renderSystem.getGBuffer();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        var sphereColliderEntities = this.getEntities(TRANSFORM, SPHERE_COLLIDER);
        for (int entityId : sphereColliderEntities) {
            debugDrawSphereCollider(entityId, camera, gBuffer);
        }

        var rigidBodyEntities = this.getEntities(TRANSFORM, RIGID_BODY);
        for (int entityId : rigidBodyEntities) {
            debugDrawRigidBody(entityId, camera, gBuffer);
        }
    }

    private void debugDrawSphereCollider(int entityId, Camera camera, GBuffer gBuffer) {
        SphereCollider sphereCollider = ecs.getComponent(SphereCollider.class, entityId);
        if (sphereCollider.debug || DebugSettings.Colliders.enabled) {
            debugDrawSphere(camera, transformSystem.getGlobalPosition(entityId), sphereCollider.radius, gBuffer, Colors.Debug.GREEN);
        }
    }

    private void debugDrawRigidBody(int entityId, Camera camera, GBuffer gBuffer) {
        RigidBody rigidBody = ecs.getComponent(RigidBody.class, entityId);
        if (DebugSettings.Physics.rigid_bodies) {
            debugDrawVector(camera, rigidBody.velocity, transformSystem.getGlobalPosition(entityId), 1, gBuffer, Colors.Debug.BLUE);
        }
    }

    private void debugDrawSphere(Camera camera, Vector3f position, float radius, GBuffer gBuffer, Vector3f color) {
        Matrix4f zAxis = new Matrix4f().translate(position);
        Matrix4f yAxis = zAxis.rotate(GraphicsMath.DEG2RAD * 90, new Vector3f(1, 0, 0), new Matrix4f());
        Matrix4f xAxis = zAxis.rotate(GraphicsMath.DEG2RAD * 90, new Vector3f(0, 1, 0), new Matrix4f());
        this.debugProgram.use();
        this.debugProgram.setCamera(camera);
        this.debugProgram.setVec3("color", color);
        this.debugProgram.setFloat("radius", radius);

        this.debugProgram.setInt("g_position", 0);
        gBuffer.bindTexture("position", GL_TEXTURE0);

        this.debugProgram.setBool("screen_space", true);
        this.debugProgram.setMat4("model", zAxis);
        circle.drawLineLoop();

        this.debugProgram.setBool("screen_space", false);

        this.debugProgram.setMat4("model", zAxis);
        circle.drawLineLoop();
        this.debugProgram.setMat4("model", yAxis);
        circle.drawLineLoop();
        this.debugProgram.setMat4("model", xAxis);
        circle.drawLineLoop();
    }

    private void debugDrawVector(Camera camera, Vector3f v, Vector3f origin, float scaleFactor, GBuffer gBuffer, Vector3f color) {
        if (v.lengthSquared() < 1e-8f) {
            return;
        }

        this.debugProgram.use();
        this.debugProgram.setCamera(camera);
        this.debugProgram.setVec3("color", color);
        this.debugProgram.setFloat("radius", 1);

        this.debugProgram.setInt("g_position", 0);
        gBuffer.bindTexture("position", GL_TEXTURE0);

        this.debugProgram.setBool("screen_space", false);

        Vector3f dir = v.normalize(new Vector3f());
        float length = v.length() * scaleFactor;
        float cone_height = Math.min(0.1f, length / 4);

        Matrix4f model = new Matrix4f()
                .translate(origin)
                .rotateTowards(dir, new Vector3f(0, 1, 0))
                // using line loop with width produced a slight protrusion at the end of the line,
                // so we scale down by half the height of the cone to hide it
                .scale(length - cone_height / 2);

        this.debugProgram.setVec3("color", color);
        this.debugProgram.setMat4("model", model);
        line.drawLineLoop();

        model = new Matrix4f()
                .translate(origin)
                .translate(dir.mul(length - cone_height))
                .rotateTowards(dir, new Vector3f(0, 1, 0))
                .rotate(GraphicsMath.DEG2RAD * 90, new Vector3f(1, 0, 0))
                .scale(cone_height);

        this.debugProgram.setVec3("color", color);
        this.debugProgram.setMat4("model", model);
        cone.draw();
    }


    @Override
    public SystemType type() {
        return SystemType.DEBUG_RENDER;
    }
}
