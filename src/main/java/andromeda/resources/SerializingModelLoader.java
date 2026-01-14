package andromeda.resources;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;

import java.nio.file.Paths;
import java.util.*;

public class SerializingModelLoader {

    private final String modelPath;
    private Map<Integer, SerializableModel> models;
    private int currentIndex = 0;

    public SerializingModelLoader(String modelPath) {
        this.modelPath = modelPath;
        this.models = new HashMap<>();
    }

    public SerializableScene load() {
        try (AIScene aiScene = Assimp.aiImportFile(modelPath,
                Assimp.aiProcess_JoinIdenticalVertices
                        | Assimp.aiProcess_Triangulate
                        | Assimp.aiProcess_GenSmoothNormals
        )) {
            return loadAiScene(aiScene);
        }
    }

    private int getNextIndex() {
        return currentIndex++;
    }

    private SerializableScene loadAiScene(AIScene aiScene) {
        SerializableModel[] m = loadModels(aiScene);
        SerializableMesh[] meshes = loadMeshes(aiScene);
        SerializableMaterial[] materials = loadMaterials(aiScene);

        return new SerializableScene(m, meshes, materials);
    }

    private SerializableModel[] loadModels(AIScene aiScene) {
        loadModel(aiScene.mRootNode(), -1);

        SerializableModel[] m = new SerializableModel[models.size()];
        models.forEach((index, model) -> m[index] = model);
        return m;
    }

    private void loadModel(AINode aiNode, int parentIndex) {
        int index = getNextIndex();
        models.put(index, modelFromAiNode(aiNode, parentIndex));

        for (int i = 0; i < aiNode.mNumChildren(); i++) {
            AINode childNode = AINode.create(aiNode.mChildren().get(i));

            loadModel(childNode, index);
        }
    }

    private SerializableModel modelFromAiNode(AINode aiNode, int parentIndex) {
        int[] meshIndices = AssimpUtil.getNodeMeshIndices(aiNode);

        Matrix4f transform = AssimpUtil.convertAiMatrix(aiNode.mTransformation());

        String name = aiNode.mName().dataString();

        return new SerializableModel(meshIndices, parentIndex, transform, name);
    }

    private SerializableMesh[] loadMeshes(AIScene aiScene) {
        SerializableMesh[] meshes = new SerializableMesh[aiScene.mNumMeshes()];
        for(int i = 0; i < meshes.length; i++) {
            var aiMesh = AIMesh.create(aiScene.mMeshes().get(i));
            var mesh = meshFromAiMesh(aiMesh);
            meshes[i] = mesh;
        }

        return meshes;
    }

    private SerializableMesh meshFromAiMesh(AIMesh aiMesh) {
        float[] vertices = AssimpUtil.getVertices(aiMesh);
        float[] normals = AssimpUtil.getNormals(aiMesh);
        float[] uvs = AssimpUtil.getUvs(aiMesh);
        int[] indices = AssimpUtil.getIndices(aiMesh);

        return new SerializableMesh(vertices, normals, uvs, indices, aiMesh.mMaterialIndex());
    }

    private SerializableMaterial[] loadMaterials(AIScene aiScene) {
        SerializableMaterial[] materials = new SerializableMaterial[aiScene.mNumMaterials()];
        for(int i = 0; i < materials.length; i++) {
            var aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));
            var material = materialFromAiMaterial(aiMaterial);
            materials[i] = material;
        }

        return materials;
    }

    private SerializableMaterial materialFromAiMaterial(AIMaterial aiMaterial) {
        String diffusePath = AssimpUtil.getDiffuseTexturePath(aiMaterial).map(s -> Paths.get(basePath(), s).toString()).orElse(null);
        String normalPath = AssimpUtil.getNormalTexturePath(aiMaterial).map(s -> Paths.get(basePath(), s).toString()).orElse(null);
        String roughnessPath = AssimpUtil.getRoughnessTexturePath(aiMaterial).map(s -> Paths.get(basePath(), s).toString()).orElse(null);

        SerializableVector3f diffuseColor = AssimpUtil.getDiffuseColor(aiMaterial).map(this::vectorFromVector3f).orElse(new SerializableVector3f(1, 1, 1));
        SerializableVector3f ambientColor = AssimpUtil.getAmbientColor(aiMaterial).map(this::vectorFromVector3f).orElse(diffuseColor);
        SerializableVector3f specularColor = AssimpUtil.getSpecularColor(aiMaterial).map(this::vectorFromVector3f).orElse(diffuseColor);

        float shininess = AssimpUtil.getShininess(aiMaterial);

        return new SerializableMaterial(diffusePath, normalPath, roughnessPath, diffuseColor, ambientColor, specularColor, shininess);
    }

    private SerializableVector3f vectorFromVector3f(Vector3f v) {
        return new SerializableVector3f(v.x, v.y, v.z);
    }

    private String basePath() {
        return Paths.get(modelPath).getParent().toString();
    }
}
