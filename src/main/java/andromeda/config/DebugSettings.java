package andromeda.config;

import org.joml.Vector3f;

public class DebugSettings {
    public static class Colliders {
        public static boolean enabled = false;
    }
    public static class Physics {
        public static boolean simulate = true;
        public static boolean rigid_bodies = true;
    }
}
