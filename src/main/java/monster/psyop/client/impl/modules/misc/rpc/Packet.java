package monster.psyop.client.impl.modules.misc.rpc;

import com.google.gson.JsonObject;

public record Packet(Opcode opcode, JsonObject data) {
}