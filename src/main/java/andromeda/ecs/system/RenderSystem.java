package andromeda.ecs.system;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.DirectionalLightComponent;
import andromeda.ecs.component.PointLightComponent;
import andromeda.ecs.component.Transform;
import andromeda.event.EventHandler;
import andromeda.framebuffer.DepthBufferArray;
import andromeda.framebuffer.FrameBuffer;
import andromeda.framebuffer.GBuffer;
import andromeda.light.DirectionalLight;
import andromeda.light.Light;
import andromeda.light.PointLight;
import andromeda.projection.Camera;
import andromeda.render.pipeline.*;
import andromeda.scene.RenderTarget;
import andromeda.util.Cascade;
import andromeda.window.Screen;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static andromeda.ecs.component.ComponentType.*;

public class RenderSystem extends EcsSystem {

    private CullPass cullPass;
    private GeometryPass geometryPass;
    private ShadowPass shadowPass;
    private LightingPass lightingPass;
    private ToneMappingPass toneMappingPass;
    private ShowPass showPass;

    private CameraSystem cameraSystem;

    public GBuffer gBuffer;
    public FrameBuffer hdrBuffer;
    public FrameBuffer depthBuffer;
    public FrameBuffer tonemappingBuffer;

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

        cullPass.init();
        geometryPass.init();
        shadowPass.init();
        lightingPass.init();
        toneMappingPass.init();
        showPass.init();

        cameraSystem = ecs.getSystem(CameraSystem.class);

        createBuffer(Screen.width, Screen.height);
        createDepthBuffer();
        EventHandler.get().addWindowResizeCallback(this::createBuffer);
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

        lightingPass.render(cascades, gBuffer, hdrBuffer, depthBuffer, this.getLights(), camera);

        toneMappingPass.render(hdrBuffer, tonemappingBuffer);


        showPass.render(tonemappingBuffer);
    }

    public int getRenderTextureId() {
        return tonemappingBuffer.renderTexture;
    }

    private void createDepthBuffer() {
        depthBuffer = DepthBufferArray.create(2048, 2048, 4);
    }

    private void createBuffer(int width, int height) {
        if (gBuffer != null)
            gBuffer.destroy();
        gBuffer = GBuffer.create(width, height);

        if (hdrBuffer != null)
            hdrBuffer.destroy();
        hdrBuffer = FrameBuffer.create(width, height, true);

        if (tonemappingBuffer != null)
            tonemappingBuffer.destroy();
        tonemappingBuffer = FrameBuffer.create(width, height);
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
