package andromeda.resources;

import andromeda.ecs.Ecs;
import andromeda.ecs.component.DirectionalLightComponent;
import andromeda.ecs.component.EcsModel;
import andromeda.ecs.component.PointLightComponent;
import andromeda.ecs.component.Transform;
import andromeda.ecs.system.TransformSystem;
import andromeda.geometry.Mesh;
import andromeda.geometry.Model;
import andromeda.geometry.Primitives;
import andromeda.light.LightType;
import andromeda.material.Material;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SceneLoader {

    public static void loadSceneEcs(String scenePath, Ecs ecs) {
        try {
            var gson = new Gson();
            var json = FileUtils.readFileToString(new File(scenePath), StandardCharsets.UTF_8);
            var sceneRepresentation = gson.fromJson(json, SceneRepresentation.class);

            sceneRepresentation.entities.forEach(rep -> loadEntity(rep, ecs));
            loadLights(sceneRepresentation, ecs);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load scene: " + scenePath);
        }
    }

    private static void loadLights(SceneRepresentation sceneRepresentation, Ecs ecs) {
        for (LightRepresentation representation : sceneRepresentation.lights) {
            if (representation.type == LightType.DIRECTIONAL) {
                int entity = ecs.createEntity();

                Transform transform = ecs.getComponent(Transform.class, entity);
                transform.rotateEuler(new Vector3f(representation.rotation));
                transform.setName("Directional Light");

                var dirLight = ecs.addComponent(DirectionalLightComponent.class, entity);
                dirLight.setColor(new Vector3f(representation.color));
                dirLight.setCastShadows(representation.castShadows);
                dirLight.intensity = representation.intensity;
            } else if (representation.type == LightType.POINT) {
                int entity = ecs.createEntity();

                Transform transform = ecs.getComponent(Transform.class, entity);
                transform.setPosition(new Vector3f(representation.position));
                transform.setName("Point Light");

                var pointLight = ecs.addComponent(PointLightComponent.class, entity);
                pointLight.setColor(new Vector3f(representation.color));
                pointLight.setRadius(representation.radius);
                pointLight.intensity = representation.intensity;
            } else {
                throw new IllegalArgumentException("Need to specify a light type!");
            }

        }
    }

    private static int loadEntity(EntityRepresentation entityRepresentation, Ecs ecs) {
        var material = Material.loadMaterial(entityRepresentation.material);

        Model model = switch (entityRepresentation.geometry) {
            case "cube" -> new Model(new Mesh(Primitives.cube(), material));
            case "plane" -> new Model(new Mesh(Primitives.plane(), material));
            default -> null;
        };

        int entityId = -1;

        if (model == null) {
            var modeLoader = new ModelLoader(entityRepresentation.geometry, ecs);
            entityId = modeLoader.load();
        } else {
            entityId = ecs.createEntity();

            var modelComponent = ecs.addComponent(EcsModel.class, entityId);
            modelComponent.getMeshes().addAll(model.getMeshes());
            modelComponent.getMeshes().forEach(m -> m.getGeometry().upload());

            Transform transformComp = ecs.getComponent(Transform.class, entityId);
            transformComp.setName(entityRepresentation.geometry);
        }

        var position = new Vector3f(entityRepresentation.position);
        var scale = new Vector3f(entityRepresentation.scale);

        Matrix4f transform = new Matrix4f();

        transform.translate(position);
        transform.scale(scale);

        Transform transformComp = ecs.getComponent(Transform.class, entityId);
        transformComp.translate(position);
        transformComp.setScale(transformComp.getScale().mul(scale));

        var transformSystem = ecs.getSystem(TransformSystem.class);

        for (var childRep : entityRepresentation.children) {
            var childEntityId = loadEntity(childRep, ecs);

            transformSystem.setParent(childEntityId, entityId);
        }

        return entityId;
    }
}
