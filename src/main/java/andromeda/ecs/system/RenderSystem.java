package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.DirectionalLightComponent;
import andromeda.ecs.component.PointLightComponent;
import andromeda.ecs.component.Transform;
import andromeda.event.EventHandler;
import andromeda.framebuffer.*;
import andromeda.input.Input;
import andromeda.input.KeyCode;
import andromeda.light.DirectionalLight;
import andromeda.light.Light;
import andromeda.light.PointLight;
import andromeda.projection.Camera;
import andromeda.render.pipeline.*;
import andromeda.scene.RenderTarget;
import andromeda.util.Cascade;
import andromeda.window.Screen;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static andromeda.ecs.component.ComponentType.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;

public class RenderSystem extends EcsSystem {

    private boolean DEBUG_SSAO = false;

    private CullPass cullPass;
    private GeometryPass geometryPass;
    private ShadowPass shadowPass;
    private LightingPass lightingPass;
    private ToneMappingPass toneMappingPass;
    private ShowPass showPass;
    private AmbientOcclusionPass ambientOcclusionPass;

    private CameraSystem cameraSystem;

    public GBuffer gBuffer;
    public ColorBuffer hdrBuffer;
    public DepthBufferArray depthBuffer;
    public ColorBuffer tonemappingBuffer;
    public ColorBuffer ambientOcclusionBuffer;
    public ColorBuffer ambientOcclusionBlurBuffer;

    public RenderSystem(Ecs ecs) {
        super(ecs);
    }

    @Override
    public void init() {
        cullPass = new CullPass(ecs);
        geometryPass = new GeometryPass();
        shadowPass = new ShadowPass();
        lightingPass = new LightingPass();
        toneMappingPass = new ToneMappingPass();
        showPass = new ShowPass(ecs);
        ambientOcclusionPass = new AmbientOcclusionPass();

        cullPass.init();
        geometryPass.init();
        shadowPass.init();
        lightingPass.init();
        toneMappingPass.init();
        showPass.init();
        ambientOcclusionPass.init();

        cameraSystem = ecs.getSystem(CameraSystem.class);

        createBuffer(Screen.width, Screen.height);
        createDepthBuffer();
        EventHandler.get().addWindowResizeCallback(this::createBuffer);
    }

    public int readEntityId(Vector2i mPos) {
        if (mPos.x > 0 && mPos.x < Screen.width && mPos.y > 0 && mPos.y < Screen.height) {
            float[] f = new float[4];
            gBuffer.readPixel(mPos.x, Screen.height - mPos.y, GL_RGBA, GL_COLOR_ATTACHMENT0, f);
            return (int) f[3];
        }
        return -1;
    }

    @Override
    public Set<Signature> getSignatures() {
        return Set.of(Signature.of(TRANSFORM, MODEL), Signature.of(TRANSFORM, POINT_LIGHT), Signature.of(TRANSFORM, DIRECTIONAL_LIGHT));
    }

    @Override
    public void update() {
        List<RenderTarget> renderTargets = cullPass.cullRenderTargets(this.getEntities(TRANSFORM, MODEL));
        Camera camera = cameraSystem.getCurrentMainCamera();
        geometryPass.render(camera, renderTargets, gBuffer);

        DirectionalLight directionalLight = getShadowCastingDirLight();
        var cascades = new Cascade[]{};
        if (directionalLight != null) {
            cascades = shadowPass.getCascades(camera, directionalLight);
            shadowPass.render(renderTargets, camera, directionalLight, depthBuffer);
        }

        ambientOcclusionPass.render(gBuffer, ambientOcclusionBuffer, ambientOcclusionBlurBuffer, camera);

        lightingPass.render(cascades, gBuffer, hdrBuffer, depthBuffer, ambientOcclusionBlurBuffer, this.getLights(), camera);

        toneMappingPass.render(hdrBuffer, tonemappingBuffer);

        if (Input.get().keyUp(KeyCode.KEY_F3)) {
            DEBUG_SSAO = !DEBUG_SSAO;
        }

        if (DEBUG_SSAO)
            showPass.render(ambientOcclusionBlurBuffer.color);
        else
            showPass.render(tonemappingBuffer.color);
    }

    private void createDepthBuffer() {
        depthBuffer = new DepthBufferArray(2048, 2048, 4);
        depthBuffer.create();
    }

    private void createBuffer(int width, int height) {
        if (gBuffer != null)
            gBuffer.destroy();
        gBuffer = new GBuffer(width, height);
        gBuffer.create();

        if (hdrBuffer != null)
            hdrBuffer.destroy();
        hdrBuffer = new ColorBuffer(width, height, GL_RGB16F, GL_RGB);
        hdrBuffer.create();

        if (tonemappingBuffer != null)
            tonemappingBuffer.destroy();
        tonemappingBuffer = new ColorBuffer(width, height, GL_RGB, GL_RGB);
        tonemappingBuffer.create();

        if (ambientOcclusionBuffer != null)
            ambientOcclusionBuffer.destroy();
        ambientOcclusionBuffer = new ColorBuffer(width, height, GL_RED, GL_RED);
        ambientOcclusionBuffer.create();

        if (ambientOcclusionBlurBuffer != null)
            ambientOcclusionBlurBuffer.destroy();
        ambientOcclusionBlurBuffer = new ColorBuffer(width, height, GL_RED, GL_RED);
        ambientOcclusionBlurBuffer.create();
    }

    private List<Light> getLights() {
        List<Light> lights = new ArrayList<>();
        for (int entity : this.getEntities(TRANSFORM, POINT_LIGHT)) {
            var pointLightComponent = ecs.getComponent(PointLightComponent.class, entity);
            var transform = ecs.getComponent(Transform.class, entity);
            lights.add(new PointLight(transform.getPosition(), pointLightComponent.getColor(), pointLightComponent.getRadius(), pointLightComponent.intensity));
        }

        for (int entity : this.getEntities(TRANSFORM, DIRECTIONAL_LIGHT)) {
            var directionalLightComponent = ecs.getComponent(DirectionalLightComponent.class, entity);
            var transform = ecs.getComponent(Transform.class, entity);
            var direction = new Vector4f(0, 1, 0, 0).mul(transform.getLocalTransform());
            var light = new DirectionalLight(direction.xyz(new Vector3f()), directionalLightComponent.getColor(), directionalLightComponent.intensity);
            light.castShadows = directionalLightComponent.castShadows;
            lights.add(light);
        }
        return lights;
    }

    private DirectionalLight getShadowCastingDirLight() {
        for (int entity : this.getEntities(TRANSFORM, DIRECTIONAL_LIGHT)) {
            var dirLight = ecs.getComponent(DirectionalLightComponent.class, entity);
            var transform = ecs.getComponent(Transform.class, entity);
            var direction = new Vector4f(0, 1, 0, 0).mul(transform.getLocalTransform());
            if (dirLight.castShadows) {
                var light = new DirectionalLight(direction.xyz(new Vector3f()), dirLight.getColor(), dirLight.intensity);
                light.castShadows = dirLight.castShadows;
                return light;
            }
        }
        return null;
    }

    @Override
    public SystemType type() {
        return SystemType.RENDER;
    }
}
