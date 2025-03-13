package andromeda.resources;

import andromeda.ecs.EcsCoordinator;
import andromeda.ecs.component.EcsModel;
import andromeda.ecs.component.Transform;
import andromeda.ecs.system.TransformSystem;
import andromeda.geometry.Model;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;

public class EcsModelLoader extends ModelLoader {
    public static int load(String path, EcsCoordinator ecsCoordinator, int level) {
        try (AIScene aiScene = Assimp.aiImportFile(path,
                Assimp.aiProcess_JoinIdenticalVertices
                        | Assimp.aiProcess_Triangulate
                        | Assimp.aiProcess_GenSmoothNormals
        )) {
            return loadAiSceneAsEntity(aiScene, path, ecsCoordinator, level);
        }
    }

    private static int loadAiSceneAsEntity(AIScene aiScene, String path, EcsCoordinator ecsCoordinator, int level) {
        return entityFromAiNode(aiScene.mRootNode(), aiScene, path, ecsCoordinator, level + 1);
    }

    private static int entityFromAiNode(AINode aiNode, AIScene aiScene, String path, EcsCoordinator ecsCoordinator, int level) {
        Matrix4f transform = convertAiMatrix(aiNode.mTransformation());

        Model model = modelFromAiNode(aiNode, aiScene, path);

        int entityId = ecsCoordinator.createEntity();

        if (!model.getMeshes().isEmpty()) {
            var modelComponent = ecsCoordinator.addComponent(EcsModel.class, entityId);
            modelComponent.getMeshes().addAll(model.getMeshes());
        }

        var transformSystem = ecsCoordinator.getSystem(TransformSystem.class);

        transformSystem.setTransform(transform, entityId);

        for (int i = 0; i < aiNode.mNumChildren(); i++) {
            AINode childNode = AINode.create(aiNode.mChildren().get(i));
            int childEntityId = entityFromAiNode(childNode, aiScene, path, ecsCoordinator, level + 1);

            transformSystem.setParent(childEntityId, entityId);
        }

        return entityId;
    }

}
