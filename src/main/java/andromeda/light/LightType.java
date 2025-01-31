package andromeda.light;

import com.google.gson.annotations.SerializedName;

public enum LightType {
    @SerializedName("directional")
    DIRECTIONAL,
    @SerializedName("point")
    POINT
}
