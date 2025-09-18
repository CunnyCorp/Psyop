package monster.psyop.client.config.sub.enums;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public enum AirPlaceMode {
    @SerializedName("horizon")
    Horizon,
    @SerializedName("on_entity")
    OnEntity,
    @SerializedName("vanilla")
    Vanilla,
    @SerializedName("none")
    None
}
