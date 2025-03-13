package andromeda.resources;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.*;
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

    public static void loadSceneEcs(String scenePath, EcsCoordinator ecsCoordinator) {
        try {
            var gson = new Gson();
            var json = FileUtils.readFileToString(new File(scenePath), StandardCharsets.UTF_8);
            var sceneRepresentation = gson.fromJson(json, SceneRepresentation.class);

            sceneRepresentation.entities.forEach(rep -> loadEntity(rep, ecsCoordinator));
            loadLights(sceneRepresentation, ecsCoordinator);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load scene: " + scenePath);
        }
    }

    private static void loadLights(SceneRepresentation sceneRepresentation, EcsCoordinator ecsCoordinator) {
        for (LightRepresentation representation : sceneRepresentation.lights) {
            if (representation.type == LightType.DIRECTIONAL) {
                int entity = ecsCoordinator.createEntity();

                ecsCoordinator.getComponent(Transform.class, entity);

                var dirLight = ecsCoordinator.addComponent(DirectionalLightComponent.class, entity);
                dirLight.setColor(new Vector3f(representation.color));
                dirLight.setDirection(new Vector3f(representation.position));
                dirLight.setCastShadows(representation.castShadows);
            } else if (representation.type == LightType.POINT) {
                int entity = ecsCoordinator.createEntity();

                ecsCoordinator.getComponent(Transform.class, entity).localTransform.translate(new Vector3f(representation.position));

                var pointLight = ecsCoordinator.addComponent(PointLightComponent.class, entity);
                pointLight.setColor(new Vector3f(representation.color));
                pointLight.setRadius(representation.radius);
            } else {
                throw new IllegalArgumentException("Need to specify a light type!");
            }

        }
    }

    private static int loadEntity(EntityRepresentation entityRepresentation, EcsCoordinator ecsCoordinator) {
        int rootEntityId = loadEntity(entityRepresentation, ecsCoordinator, 0);
        return rootEntityId;
    }

    private static int loadEntity(EntityRepresentation entityRepresentation, EcsCoordinator ecsCoordinator, int level) {
        var material = Material.loadMaterial(entityRepresentation.material);

        Model model = switch (entityRepresentation.geometry) {
            case "cube" -> new Model(new Mesh(Primitives.cube(), material));
            case "plane" -> new Model(new Mesh(Primitives.plane(), material));
            default -> null;
        };

        int entityId = -1;

        if (model == null) {
            entityId = EcsModelLoader.load(entityRepresentation.geometry, ecsCoordinator, level);
        } else {
            entityId = ecsCoordinator.createEntity();

            var modelComponent = ecsCoordinator.addComponent(EcsModel.class, entityId);
            modelComponent.getMeshes().addAll(model.getMeshes());
            modelComponent.getMeshes().forEach(m -> m.getGeometry().upload());
        }

        var position = new Vector3f(entityRepresentation.position);
        var scale = new Vector3f(entityRepresentation.scale);

        Matrix4f transform = new Matrix4f();

        transform.translate(position);
        transform.scale(scale);

        var transformSystem = ecsCoordinator.getSystem(TransformSystem.class);

        transformSystem.setTransform(transform, entityId);

        for (var childRep : entityRepresentation.children) {
            var childEntityId = loadEntity(childRep, ecsCoordinator, level + 1);
            var childTransform = ecsCoordinator.getComponent(Transform.class, childEntityId);

            transformSystem.setParent(childEntityId, entityId);
        }

        return entityId;
    }
}
