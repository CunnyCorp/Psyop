package monster.psyop.client.config.sub.enums;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public enum ActionMode {
    @SerializedName("strict")
    Strict,
    @SerializedName("raytrace")
    Raytrace,
    @SerializedName("normal")
    Normal
}
