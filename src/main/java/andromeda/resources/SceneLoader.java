package andromeda.resources;

import andromeda.ecs.Ecs;
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
import org.joml.Quaternionf;
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

                var rotation = representation.rotation;
                Quaternionf qRotation = new Quaternionf();
                qRotation.rotateLocalX((float)Math.toRadians(rotation[0]));
                qRotation.rotateLocalY((float)Math.toRadians(rotation[1]));
                qRotation.rotateLocalZ((float)Math.toRadians(rotation[2]));
                ecs.getComponent(Transform.class, entity).localTransform.rotate(qRotation);

                var dirLight = ecs.addComponent(DirectionalLightComponent.class, entity);
                dirLight.setColor(new Vector3f(representation.color));
                dirLight.setCastShadows(representation.castShadows);
            } else if (representation.type == LightType.POINT) {
                int entity = ecs.createEntity();

                ecs.getComponent(Transform.class, entity).localTransform.translate(new Vector3f(representation.position));

                var pointLight = ecs.addComponent(PointLightComponent.class, entity);
                pointLight.setColor(new Vector3f(representation.color));
                pointLight.setRadius(representation.radius);
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
        }

        var position = new Vector3f(entityRepresentation.position);
        var scale = new Vector3f(entityRepresentation.scale);

        Matrix4f transform = new Matrix4f();

        transform.translate(position);
        transform.scale(scale);

        var transformSystem = ecs.getSystem(TransformSystem.class);

        transformSystem.setTransform(transform, entityId);

        for (var childRep : entityRepresentation.children) {
            var childEntityId = loadEntity(childRep, ecs);

            transformSystem.setParent(childEntityId, entityId);
        }

        return entityId;
    }
}
