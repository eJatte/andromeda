package andromeda;

import andromeda.resources.SerializableScene;
import andromeda.resources.SerializingModelLoader;

import java.io.*;

public class SerializeModel {

    public void loadModels(String path) throws IOException, ClassNotFoundException {
        var modelLoader = new SerializingModelLoader(path);
        SerializableScene scene = modelLoader.load();

        FileOutputStream fileOutputStream
                = new FileOutputStream("sponza.scene");
        ObjectOutputStream objectOutputStream
                = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(scene);
        objectOutputStream.flush();
        objectOutputStream.close();

        FileInputStream fileInputStream
                = new FileInputStream("sponza.scene");
        ObjectInputStream objectInputStream
                = new ObjectInputStream(fileInputStream);
        SerializableScene scene_input = (SerializableScene) objectInputStream.readObject();

        objectInputStream.close();
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length == 0) {
            System.err.println("Please specify a model path");

        } else {
            new SerializeModel().loadModels(args[0]);
        }
    }
}
