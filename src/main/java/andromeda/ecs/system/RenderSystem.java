package andromeda.ecs.system;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.*;
import andromeda.framebuffer.DepthBufferArray;
import andromeda.framebuffer.FrameBuffer;
import andromeda.geometry.Geometry;
import andromeda.geometry.Mesh;
import andromeda.geometry.Primitives;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.light.DirectionalLight;
import andromeda.light.Light;
import andromeda.light.PointLight;
import andromeda.projection.Camera;
import andromeda.projection.OrthographicCamera;
import andromeda.scene.RenderTarget;
import andromeda.shader.Program;
import andromeda.util.Cascade;
import andromeda.util.GraphicsMath;
import andromeda.window.Screen;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE3;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;

public class RenderSystem extends EcsSystem {

    private static final int CASCADE_LEVELS = 4;
    private static final int SHADOW_MAP_SIZE = 2048;
    private CameraSystem cameraSystem;
    private TransformSystem transformSystem;
    private FrameBuffer frameBuffer;
    private FrameBuffer depthBufferCascade;
    private Program defaultProgram, normalDebugProgram, unlitProgram;
    private Program shadowCascadeProgram;
    private Program renderTextureProgram;
    private Geometry quad;

    private static Set<Integer> renderEntities, pointLightEntities, directionalLightEntities;
    private boolean DEBUG_HIERACHY = false, DEBUG_FRUSTUM = false, DEBUG_NORMALS = false;

    public RenderSystem(EcsCoordinator ecsCoordinator) {
        super(List.of(ComponentType.TRANSFORM), ecsCoordinator);
        renderEntities = new HashSet<>();
        pointLightEntities = new HashSet<>();
        directionalLightEntities = new HashSet<>();
    }

    @Override
    public void addEntity(int entityId) {
        if (ecsCoordinator.getSignature(entityId).get(ComponentType.MODEL.id)) {
            renderEntities.add(entityId);
        }
        if (ecsCoordinator.getSignature(entityId).get(ComponentType.POINT_LIGHT.id)) {
            pointLightEntities.add(entityId);
        }
        if (ecsCoordinator.getSignature(entityId).get(ComponentType.DIRECTIONAL_LIGHT.id)) {
            directionalLightEntities.add(entityId);
        }
    }

    @Override
    public void removeEntity(int entityId) {
        if (ecsCoordinator.getSignature(entityId).get(ComponentType.MODEL.id)) {
            renderEntities.remove(entityId);
        }
        if (ecsCoordinator.getSignature(entityId).get(ComponentType.POINT_LIGHT.id)) {
            pointLightEntities.remove(entityId);
        }
        if (ecsCoordinator.getSignature(entityId).get(ComponentType.DIRECTIONAL_LIGHT.id)) {
            directionalLightEntities.remove(entityId);
        }
    }

    @Override
    public void update() {
        if (Input.get().keyUp(KeyCode.KEY_I)) {
            DEBUG_HIERACHY = !DEBUG_HIERACHY;
        }
        if (Input.get().keyUp(KeyCode.KEY_T)) {
            DEBUG_FRUSTUM = !DEBUG_FRUSTUM;
        }
        if (Input.get().keyUp(KeyCode.KEY_N)) {
            DEBUG_NORMALS = !DEBUG_NORMALS;
        }

        render();
    }

    @Override
    public void init() {
        cameraSystem = ecsCoordinator.getSystem(CameraSystem.class);
        transformSystem = ecsCoordinator.getSystem(TransformSystem.class);

        initFrameBuffer();
        initShaders();
        initGL();
    }

    public int getRenderTextureId() {
        return frameBuffer.renderTexture;
    }

    private void render() {
        var camera = cameraSystem.getCurrentMainCamera();

        var cascades = new Cascade[]{};
        var renderTargets = getRenderTargets();

        var shadowCaster = getShadowCastingDirLight();
        if(shadowCaster != null) {
            cascades = getShadowCascades(camera, shadowCaster, CASCADE_LEVELS, new float[]{0.1f, 0.3f, 0.6f, 1.0f});
            this.renderShadow(renderTargets, cascades, depthBufferCascade, this.shadowCascadeProgram);
        }

        this.render(renderTargets, camera, cascades, getLights(), frameBuffer);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        glViewport(0, 0, Screen.width, Screen.height);

        if(this.ecsCoordinator.getSystem(PropertiesSystem.class).isPlayMode()) {
            renderTextureProgram.use();

            renderTextureProgram.setInt("renderedTexture", 0);
            frameBuffer.bindTexture(GL_TEXTURE0);

            quad.draw();
        }
    }

    private DirectionalLight getShadowCastingDirLight() {
        for (int entity : directionalLightEntities) {
            var dir = ecsCoordinator.getComponent(DirectionalLightComponent.class, entity);
            if(dir.castShadows) {
                var light = new DirectionalLight(dir.getDirection(), dir.getColor());
                light.castShadows = dir.castShadows;
                return light;
            }
        }
        return null;
    }

    private List<Light> getLights() {
        List<Light> lights = new ArrayList<>();
        for (int entity : pointLightEntities) {
            var pointLightComponent = ecsCoordinator.getComponent(PointLightComponent.class, entity);
            var transform = ecsCoordinator.getComponent(Transform.class, entity);
            lights.add(new PointLight(transform.getPosition(), pointLightComponent.getColor(), pointLightComponent.getRadius()));
        }

        for (int entity : directionalLightEntities) {
            var directionalLightComponent = ecsCoordinator.getComponent(DirectionalLightComponent.class, entity);
            var light = new DirectionalLight(directionalLightComponent.getDirection(), directionalLightComponent.getColor());
            light.castShadows = directionalLightComponent.castShadows;
            lights.add(light);
        }
        return lights;
    }

    private void render(List<RenderTarget> renderTargets, Camera camera, Cascade[] cascades, List<Light> lights, FrameBuffer targetBuffer) {
        targetBuffer.bind();

        for (var target : renderTargets) {
            if (target.getMesh().getMaterial().unlit) {
                this.render(target, camera, this.unlitProgram, cascades, lights);
            } else {
                this.render(target, camera, this.defaultProgram, cascades, lights);
            }
            if (this.DEBUG_NORMALS)
                this.render(target, camera, this.normalDebugProgram, cascades, lights);
        }
    }

    private void render(RenderTarget renderTarget, Camera camera, Program program, Cascade[] cascades, List<Light> lights) {
        var mesh = renderTarget.getMesh();
        var model = renderTarget.getTransform();
        var geometry = mesh.getGeometry();

        program.use();
        program.setCamera(camera);
        program.setLights("lights", lights);

        program.setMaterial("material", mesh.getMaterial());
        program.setMat4("model", model);

        program.setCascades(cascades);

        program.setInt("shadow_map", 3);
        depthBufferCascade.bindTexture(GL_TEXTURE3);

        if (mesh.getMaterial().wireFrame) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glDisable(GL_CULL_FACE);
        }

        geometry.draw();

        if (mesh.getMaterial().wireFrame) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glEnable(GL_CULL_FACE);
        }
    }

    private Cascade[] getShadowCascades(Camera camera, DirectionalLight directionalLight, int n_levels, float[] levels) {
        if(n_levels != levels.length) {
            throw new IllegalArgumentException("Wrong number of levels in levels array, expected %d".formatted(n_levels));
        }

        var near = camera.getNear();
        var far = camera.getFar();
        var view = camera.getView();

        var shadowDir = directionalLight.position;

        Cascade[] cascades = new Cascade[n_levels];

        float prev_far = near;
        for (int i = 0; i < n_levels; i++) {
            float level_i_far = (far * levels[i]);
            Matrix4f level_i_frustum = camera.getProjection(prev_far, level_i_far);
            OrthographicCamera level_i_ortho = directionalOrtho(shadowDir, level_i_frustum, view);

            cascades[i] = new Cascade(level_i_ortho.getProjectionView(), levels[i], level_i_ortho.getLeft());

            prev_far = level_i_far;
        }

        return cascades;
    }

    private void renderShadow(List<RenderTarget> renderTargets, Cascade[] cascades, FrameBuffer targetBuffer, Program program) {
        targetBuffer.bind();

        for (var target : renderTargets) {
            if (!target.getMesh().getMaterial().unlit) {
                this.renderShadow(target, cascades, program);
            }
        }
    }

    private void renderShadow(RenderTarget renderTarget, Cascade[] cascades, Program program) {
        var mesh = renderTarget.getMesh();
        var model = renderTarget.getTransform();
        var geometry = mesh.getGeometry();

        program.use();
        program.setCascades(cascades);
        program.setMat4("model", model);

        geometry.draw();
    }

    private List<RenderTarget> getRenderTargets() {
        List<RenderTarget> renderTargets = new ArrayList<>();

        for (var entityId : renderEntities) {
            var model = ecsCoordinator.getComponent(EcsModel.class, entityId);
            if (model != null) {
                for (Mesh mesh : model.getMeshes()) {
                    renderTargets.add(new RenderTarget(mesh, transformSystem.getGlobalTransform(entityId)));
                }
            }
        }
        return renderTargets;
    }



    private void initFrameBuffer() {
        frameBuffer = FrameBuffer.create(Screen.width, Screen.height);
        int shadowMapSize = SHADOW_MAP_SIZE;
        depthBufferCascade = DepthBufferArray.create(shadowMapSize, shadowMapSize, CASCADE_LEVELS);
        quad = Primitives.quad();
        quad.upload();
    }

    private void initShaders() {
        this.defaultProgram = Program.loadShader("shaders/phong.vert", "shaders/phong.frag");
        this.normalDebugProgram = Program.loadShader("shaders/normal_debug.vert", "shaders/normal_debug.frag", "shaders/normal_debug.geom");
        this.unlitProgram = Program.loadShader("shaders/unlit.vert", "shaders/unlit.frag");
        this.shadowCascadeProgram = Program.loadShader("shaders/shadow_cascade.vert", "shaders/shadow_cascade.frag", "shaders/shadow_cascade.geom");
        this.renderTextureProgram = Program.loadShader("shaders/framebuffer.vert", "shaders/framebuffer.frag");
    }

    private void initGL() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    private OrthographicCamera directionalOrtho(Vector3f dir, Matrix4f projection, Matrix4f view) {
        var boundingSphere = GraphicsMath.getBoundingSphere(projection, view);

        var orthoCamera = new OrthographicCamera(boundingSphere.radius, boundingSphere.radius);
        orthoCamera.setPosition(dir.mul(100, new Vector3f()).add(new Vector3f(0)));
        orthoCamera.lookAtPoint(new Vector3f(0));
        orthoCamera.setFar(200);

        var orthoView = orthoCamera.getView();
        var inverseOrthoView = orthoView.invertAffine(new Matrix4f());

        var radius = boundingSphere.radius;
        var min_size = radius / SHADOW_MAP_SIZE * 2;

        var shadowSpacePos = new Vector4f(boundingSphere.position, 1).mul(orthoView);
        shadowSpacePos = new Vector4f(shadowSpacePos.x - shadowSpacePos.x % min_size, shadowSpacePos.y - shadowSpacePos.y % min_size, shadowSpacePos.z, shadowSpacePos.w);
        var snappedWorldSpacePos = shadowSpacePos.mul(inverseOrthoView, new Vector4f()).xyz(new Vector3f());

        orthoCamera.setPosition(dir.mul(100, new Vector3f()).add(snappedWorldSpacePos));
        orthoCamera.lookAtPoint(snappedWorldSpacePos);

        return orthoCamera;
    }

    @Override
    public SystemType type() {
        return SystemType.RENDER;
    }

}
