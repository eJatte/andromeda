package andromeda.resources;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.assimp.Assimp.*;

public class AssimpUtil {
    public static List<AIMesh> getAIMeshes(AINode aiNode, AIScene aiScene) {
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

    public static int[] getNodeMeshIndices(AINode aiNode) {
        int[] mesh_indices = new int[aiNode.mNumMeshes()];

        for (int i = 0; i < mesh_indices.length; i++) {
            mesh_indices[i] = aiNode.mMeshes().get(i);
        }
        return mesh_indices;
    }

    public static float[] getVertices(AIMesh aiMesh) {

        return readAiVector3DBuffer(aiMesh.mVertices(), aiMesh.mNumVertices());
    }

    public static float[] getNormals(AIMesh aiMesh) {
        var normals = aiMesh.mNormals();
        return normals != null ? readAiVector3DBuffer(normals, aiMesh.mNumVertices()) : new float[0];
    }

    public static float[] getUvs(AIMesh aiMesh) {
        // TO-DO check if we have multiple texture coords
        var textureCoords = aiMesh.mTextureCoords(0);
        return textureCoords != null ? readAiVector3DBufferTo2D(textureCoords, aiMesh.mNumVertices()) : new float[0];
    }

    public static int[] getIndices(AIMesh aiMesh) {
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        List<Integer> indices = new ArrayList<>();

        while (aiFaces.remaining() > 0) {
            AIFace aiFace = aiFaces.get();
            for (int f = 0; f < aiFace.mNumIndices(); f++) {
                var index = aiFace.mIndices().get(f);
                indices.add(index);
            }
        }

        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    public static Matrix4f convertAiMatrix(AIMatrix4x4 aiMatrix) {
        Matrix4f matrix = new Matrix4f();
        matrix.setTransposedFromAddress(aiMatrix.address());
        return matrix;
    }

    public static float getShininess(AIMaterial aiMaterial) {
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

    public static Optional<Vector3f> getDiffuseColor(AIMaterial aiMaterial) {
        return getColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE);
    }

    public static Optional<Vector3f> getAmbientColor(AIMaterial aiMaterial) {
        return getColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT);
    }

    public static Optional<Vector3f> getSpecularColor(AIMaterial aiMaterial) {
        return getColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR);
    }

    private static Optional<Vector3f> getColor(AIMaterial aiMaterial, String key) {
        AIColor4D aiColor = AIColor4D.create();

        int result = aiGetMaterialColor(aiMaterial, key, aiTextureType_NONE, 0, aiColor);

        return result == 0 ? Optional.of(new Vector3f(aiColor.r(), aiColor.g(), aiColor.b())) : Optional.empty();
    }

    public static Optional<String> getDiffuseTexturePath(AIMaterial aiMaterial) {
        return getTexturePath(aiMaterial, aiTextureType_DIFFUSE);
    }

    public static Optional<String> getNormalTexturePath(AIMaterial aiMaterial) {
        return getTexturePath(aiMaterial, aiTextureType_NORMALS);
    }

    public static Optional<String> getRoughnessTexturePath(AIMaterial aiMaterial) {
        return getTexturePath(aiMaterial, aiTextureType_DIFFUSE_ROUGHNESS);
    }

    private static Optional<String> getTexturePath(AIMaterial aiMaterial, int key) {
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

    private static float[] readAiVector3DBuffer(AIVector3D.Buffer buffer, int nVertices) {
        float[] v = new float[nVertices * 3];
        int index = 0;

        while (buffer.remaining() > 0) {
            AIVector3D aiVertex = buffer.get();
            v[index] = aiVertex.x();
            v[index + 1] = aiVertex.y();
            v[index + 2] = aiVertex.z();

            index += 3;
        }

        return v;
    }

    private static float[] readAiVector3DBufferTo2D(AIVector3D.Buffer buffer, int nVertices) {
        float[] v = new float[nVertices * 2];
        int index = 0;

        while (buffer.remaining() > 0) {
            AIVector3D aiVertex = buffer.get();
            v[index] = aiVertex.x();
            v[index + 1] = aiVertex.y();

            index += 2;
        }

        return v;
    }
}
