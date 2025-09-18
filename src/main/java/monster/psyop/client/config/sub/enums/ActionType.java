package monster.psyop.client.config.sub.enums;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public enum ActionType {
    @SerializedName("packet")
    Packet,
    @SerializedName("vanilla")
    Vanilla,
    @SerializedName("both")
    Both
}
