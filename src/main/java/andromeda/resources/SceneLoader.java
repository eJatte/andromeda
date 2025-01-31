package andromeda.resources;

import andromeda.entity.Entity;
import andromeda.scene.Scene;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SceneLoader {
    public static Scene loadScene(String scenePath) {
        try {
            var gson = new Gson();
            var json = FileUtils.readFileToString(new File(scenePath), StandardCharsets.UTF_8);
            var sceneRepresentation = gson.fromJson(json, SceneRepresentation.class);

            var entities = sceneRepresentation.entities.stream().map(Entity::loadEntity).toList();
            var lights = sceneRepresentation.lights.stream().map(LightLoader::loadLight).toList();

            return new Scene(entities, lights);

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load scene: " + scenePath);
        }
    }
}
