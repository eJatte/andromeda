package andromeda.resources;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.EcsModel;
import andromeda.ecs.system.TransformSystem;
import andromeda.geometry.Geometry;
import andromeda.geometry.Mesh;
import andromeda.geometry.Model;
import andromeda.material.Material;
import andromeda.material.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

public class ModelLoader {

    private final String modelPath;
    private final EcsCoordinator ecsCoordinator;
    private Material[] materials;
    private Mesh[] meshes;

    public ModelLoader(String modelPath, EcsCoordinator ecsCoordinator) {
        this.modelPath = modelPath;
        this.ecsCoordinator = ecsCoordinator;
    }

    public int load() {
        var modelLoader = new SerializingModelLoader(modelPath);
        SerializableScene scene = modelLoader.load();

        materials = new Material[scene.materials().length];
        meshes = new Mesh[scene.meshes().length];

        return loadScene(scene);
    }

    private int loadScene(SerializableScene scene) {
        loadMaterials(scene.materials());
        loadMeshes(scene.meshes());
        return loadModels(scene.models());
    }

    private int loadModels(SerializableModel[] s_models) {
        int[] entities = new int[s_models.length];

        var transformSystem = ecsCoordinator.getSystem(TransformSystem.class);

        for (int i = 0; i < entities.length; i++) {
            SerializableModel sModel = s_models[i];
            Model model = serializableModelToModel(sModel);
            int entityId = ecsCoordinator.createEntity();

            if (!model.getMeshes().isEmpty()) {
                var modelComponent = ecsCoordinator.addComponent(EcsModel.class, entityId);
                modelComponent.setMeshes(model.getMeshes());
            }

            transformSystem.setTransform(model.getTransform(), entityId);

            entities[i] = entityId;
        }


        var parentMap = getParentMap(s_models);
        int rootEntity = -1;

        for (int i = 0; i < entities.length; i++) {
            int entityId = entities[i];
            int parentIndex = parentMap.get(i);
            if (parentIndex == -1) {
                rootEntity = entityId;
            } else {
                transformSystem.setParent(entityId, entities[parentIndex]);
            }
        }

        return rootEntity;
    }

    private static Map<Integer, Integer> getParentMap(SerializableModel[] models) {
        Map<Integer, Integer> parentMap = new HashMap<>();
        for (int i = 0; i < models.length; i++) {
            parentMap.put(i, models[i].parentIndex());
        }
        return parentMap;
    }

    private Model serializableModelToModel(SerializableModel model) {
        List<Mesh> meshes = new ArrayList<>();

        for (var mesh_index : model.mesh_indices()) {
            meshes.add(this.meshes[mesh_index]);
        }

        return new Model(meshes, model.transform());
    }

    private void loadMeshes(SerializableMesh[] s_meshes) {
        for(int i = 0; i < s_meshes.length; i++) {
            meshes[i] = serializableMeshToMesh(s_meshes[i]);
        }
    }

    private void loadMaterials(SerializableMaterial[] s_materials) {
        for(int i = 0; i < s_materials.length; i++) {
            materials[i] = serializableMaterialToMaterial(s_materials[i]);
        }
    }

    private Mesh serializableMeshToMesh(SerializableMesh mesh) {
        Vector3f[] vertices = floatArrayToVector3f(mesh.vertices());
        Vector3f[] normals = floatArrayToVector3f(mesh.normals());
        Vector2f[] uvs = floatArrayToVector2f(mesh.uvs());
        int[] indices = mesh.indices();

        Geometry geometry = new Geometry(vertices, normals, uvs, indices);
        geometry.upload();

        Material material = materials[mesh.material_index()];

        return new Mesh(geometry, material);
    }

    private Vector3f[] floatArrayToVector3f(float[] floats) {
        Vector3f[] vectors = new Vector3f[floats.length / 3];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = new Vector3f(floats[i * 3], floats[i * 3 + 1], floats[i * 3 + 2]);
        }
        return vectors;
    }

    private Vector2f[] floatArrayToVector2f(float[] floats) {
        Vector2f[] vectors = new Vector2f[floats.length / 2];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = new Vector2f(floats[i * 2], floats[i * 2 + 1]);
        }
        return vectors;
    }

    private Material serializableMaterialToMaterial(SerializableMaterial serializableMaterial) {
        Material material = new Material();

        material.diffuse = serializableMaterial.diffuseColor().getVector3f();
        material.ambient = serializableMaterial.ambientColor().getVector3f();
        material.specular = serializableMaterial.specularColor().getVector3f();

        material.shininess = serializableMaterial.shininess();

        material.diffuse_texture = loadAsTexture(serializableMaterial.diffusePath());
        material.normal_texture = loadAsNormalTexture(serializableMaterial.normalPath());
        material.roughness_texture = loadAsTexture(serializableMaterial.roughnessPath());

        return material;
    }

    private Texture loadAsTexture(String texturePath) {
        return Optional.ofNullable(texturePath).map(Texture::loadTexture).orElse(null);
    }

    private Texture loadAsNormalTexture(String texturePath) {
        return Optional.ofNullable(texturePath).map(Texture::loadNormalTexture).orElse(null);
    }
}
