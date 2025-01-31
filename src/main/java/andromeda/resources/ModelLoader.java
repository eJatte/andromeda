package andromeda.resources;

import andromeda.entity.Entity;
import andromeda.geometry.Geometry;
import andromeda.geometry.Mesh;
import andromeda.geometry.Model;
import andromeda.material.Material;
import andromeda.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.assimp.Assimp.*;

public class ModelLoader {
    public static Entity load(String path, Material material) {
        try (AIScene aiScene = Assimp.aiImportFile(path,
                Assimp.aiProcess_JoinIdenticalVertices
                        | Assimp.aiProcess_Triangulate
                        | Assimp.aiProcess_GenSmoothNormals
        )) {
            return loadAiSceneAsEntity(aiScene, path, material);
        }
    }

    private static Entity loadAiSceneAsEntity(AIScene aiScene, String path, Material material) {
        return entityFromAiNode(aiScene.mRootNode(), aiScene, path, material);
    }

    private static Entity entityFromAiNode(AINode aiNode, AIScene aiScene, String path, Material material) {
        Matrix4f transform = convertAiMatrix(aiNode.mTransformation());
        Model model = modelFromAiNode(aiNode, aiScene, path, material);
        Entity entity = new Entity(model, (e) -> {
        });
        entity.transform().set(transform);

        for (int i = 0; i < aiNode.mNumChildren(); i++) {
            AINode childNode = AINode.create(aiNode.mChildren().get(i));
            Entity childEntity = entityFromAiNode(childNode, aiScene, path, material);
            entity.children().add(childEntity);
        }

        return entity;
    }

    private static Model modelFromAiNode(AINode aiNode, AIScene aiScene, String path, Material material) {
        List<AIMesh> aiMeshes = getAIMeshes(aiNode, aiScene);

        List<Mesh> meshes = new ArrayList<>();

        for (var aiMesh : aiMeshes) {

            var indices = getIndices(aiMesh, 0);
            var vertices = getVertices(aiMesh);
            var normals = getNormals(aiMesh);
            var uvs = getUvs(aiMesh);

            Vector3f[] vertices_array = vertices.toArray(Vector3f[]::new);
            Vector3f[] normals_array = normals.toArray(Vector3f[]::new);
            int[] indices_array = indices.stream().mapToInt(Integer::intValue).toArray();
            Vector2f[] uvs_array = uvs.toArray(Vector2f[]::new);

            var geometry = new Geometry(vertices_array, normals_array, uvs_array, indices_array);
            geometry.upload();

            var material_index = aiMesh.mMaterialIndex();
            AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(material_index));

            var m = getMaterial(aiMaterial, Paths.get(path).getParent().toString());
            m.program = material.program;

            if (m.isTransparent) {
                System.out.println("Material is transparent skipping model...");
            } else {
                Mesh mesh = new Mesh(geometry, m);
                meshes.add(mesh);
            }
        }

        return new Model(meshes);
    }

    private static List<AIMesh> getAIMeshes(AINode aiNode, AIScene aiScene) {
        AIMesh[] sceneMeshes = new AIMesh[aiScene.mNumMeshes()];

        for (int i = 0; i < aiScene.mNumMeshes(); i++) {
            sceneMeshes[i] = AIMesh.create(aiScene.mMeshes().get(i));
        }

        List<AIMesh> meshes = new ArrayList<>();

        for (int i = 0; i < aiNode.mNumMeshes(); i++) {
            meshes.add(sceneMeshes[aiNode.mMeshes().get(i)]);
        }
        return meshes;
    }

    private static Matrix4f convertAiMatrix(AIMatrix4x4 aiMatrix) {
        Matrix4f matrix = new Matrix4f();
        matrix.setTransposedFromAddress(aiMatrix.address());
        return matrix;
    }

    private static List<Vector3f> getVertices(AIMesh aiMesh) {
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        List<Vector3f> vertices = new ArrayList<>();

        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();
            vertices.add(new Vector3f(aiVertex.x(), aiVertex.y(), aiVertex.z()));
        }

        return vertices;
    }

    private static List<Vector3f> getNormals(AIMesh aiMesh) {
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        List<Vector3f> normals = new ArrayList<>();

        while (aiNormals.remaining() > 0) {
            AIVector3D aiNormal = aiNormals.get();
            normals.add(new Vector3f(aiNormal.x(), aiNormal.y(), aiNormal.z()));
        }

        return normals;
    }

    private static List<Vector2f> getUvs(AIMesh aiMesh) {
        // TO-DO check if we have multiple texture coords
        AIVector3D.Buffer aiTextureCoords = aiMesh.mTextureCoords(0);
        List<Vector2f> uvs = new ArrayList<>();

        while (aiTextureCoords.remaining() > 0) {
            AIVector3D aiTexCoord = aiTextureCoords.get();
            uvs.add(new Vector2f(aiTexCoord.x(), aiTexCoord.y()));
        }

        return uvs;
    }

    private static List<Integer> getIndices(AIMesh aiMesh, int offset) {
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        List<Integer> indices = new ArrayList<>();

        while (aiFaces.remaining() > 0) {
            AIFace aiFace = aiFaces.get();
            for (int f = 0; f < aiFace.mNumIndices(); f++) {
                var index = aiFace.mIndices().get(f);
                indices.add(index + offset);
            }
        }

        return indices;
    }


    private static Material getMaterial(AIMaterial aiMaterial, String basePath) {
        Material material = new Material();

        material.diffuse = getDiffuseColor(aiMaterial);
        material.ambient = getAmbientColor(aiMaterial);
        if (material.ambient.x() < 0) {
            material.ambient = material.diffuse;
        }
        material.specular = getSpecularColor(aiMaterial);
        if (material.specular.x() < 0) {
            material.specular = material.diffuse;
        }
        material.shininess = getShininess(aiMaterial);

        material.diffuse_texture = getDiffuseTexture(aiMaterial, basePath);
        material.normal_texture = getNormalTexture(aiMaterial, basePath);
        material.roughness_texture = getRoughnessTexture(aiMaterial, basePath);

        //material.isTransparent = hasTransparency(aiMaterial);

        return material;
    }

    private static Vector3f getDiffuseColor(AIMaterial aiMaterial) {
        return getColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE);
    }

    private static Vector3f getAmbientColor(AIMaterial aiMaterial) {
        return getColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT);
    }

    private static Vector3f getSpecularColor(AIMaterial aiMaterial) {
        return getColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR);
    }

    private static Vector3f getColor(AIMaterial aiMaterial, String key) {
        AIColor4D aiColor = AIColor4D.create();

        Vector3f color = Material.NO_COLOR;
        int result = aiGetMaterialColor(aiMaterial, key, aiTextureType_NONE, 0, aiColor);
        if (result == 0) {
            color = new Vector3f(aiColor.r(), aiColor.g(), aiColor.b());
        }

        return color;
    }

    private static float getShininess(AIMaterial aiMaterial) {
        float shininess = getFloatProperty(aiMaterial, AI_MATKEY_SHININESS);
        return shininess == 0 ? 8 : shininess;
    }

    private static float getFloatProperty(AIMaterial aiMaterial, String key) {
        var floatBuffer = BufferUtils.createFloatBuffer(1);
        var intBuffer = BufferUtils.createIntBuffer(1);

        intBuffer.put(1);
        intBuffer.flip();

        int result = aiGetMaterialFloatArray(aiMaterial, key, aiTextureType_NONE, 0, floatBuffer, intBuffer);
        float propertyValue = 0;
        if (result == 0) {
            propertyValue = floatBuffer.get(0);
        }

        return propertyValue;
    }

    private static boolean hasTransparency(AIMaterial aiMaterial) {
        var ai_string = AIString.create();
        // only a hack for now to skip decals in the sponza model
        // remove once we have support for alpha textures
        int res = aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, ai_string, (IntBuffer) null, null, null, null, null, null);
        return res != 0;
    }

    private static Texture getDiffuseTexture(AIMaterial aiMaterial, String basePath) {
        var path = getTexturePath(aiMaterial, basePath, aiTextureType_DIFFUSE);
        return path.map(s -> Texture.loadTexture(Paths.get(basePath, s).toString())).orElse(null);
    }

    private static Texture getNormalTexture(AIMaterial aiMaterial, String basePath) {
        var path = getTexturePath(aiMaterial, basePath, aiTextureType_NORMALS);
        return path.map(s -> Texture.loadNormalTexture(Paths.get(basePath, s).toString())).orElse(null);
    }

    private static Texture getRoughnessTexture(AIMaterial aiMaterial, String basePath) {
        var path = getTexturePath(aiMaterial, basePath, aiTextureType_DIFFUSE_ROUGHNESS);
        return path.map(s -> Texture.loadTexture(Paths.get(basePath, s).toString())).orElse(null);
    }

    private static Optional<String> getTexturePath(AIMaterial aiMaterial, String basePath, int key) {
        var ai_string = AIString.create();

        // see the "constants" table to see what all these do
        // https://assimp-docs.readthedocs.io/en/latest/usage/use_the_lib.html#access-by-c-class-interface

        int[] texture_mapping = new int[1]; // aiTextureMapping
        int[] uv_index = new int[1];
        float[] blend_strength = new float[1];
        int[] operation = new int[1]; // aiTextureOp
        int[] texture_map_mode = new int[1]; // aiTextureMapMode

        int res = aiGetMaterialTexture(aiMaterial, key, 0, ai_string, texture_mapping, uv_index, blend_strength, operation, texture_map_mode, null);

        return res == 0 ? Optional.of(ai_string.dataString()) : Optional.empty();
    }

}
