package andromeda.resources;

import andromeda.light.LightType;

public class LightRepresentation {
    public float[] position, rotation, color;
    public LightType type;
    public float radius = 1;
    public float intensity = 1;
    public boolean castShadows;
}
