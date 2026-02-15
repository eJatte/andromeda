package andromeda.config;

import org.joml.Vector3f;

public class GraphicsSettings {
    public class AmbientOcclusion {

        public static float radius = 0.3f;
        public static float power = 1.0f;
        public static int n_samples = 64;
        public static float bias = 0.02f;
    }

    public class Fog {
        public static Vector3f color = new Vector3f(0.3f, 0.5f, 0.6f);
        public static float depth = 100;
        public static float power = 3;
    }
}
