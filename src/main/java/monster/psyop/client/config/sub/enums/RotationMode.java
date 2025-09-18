package monster.psyop.client.config.sub.enums;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public enum RotationMode {
    @SerializedName("meteor")
    Default,
    @SerializedName("ncp")
    NCPBypass,
    @SerializedName("packet")
    Packet,
    @SerializedName("disabled")
    None
}
