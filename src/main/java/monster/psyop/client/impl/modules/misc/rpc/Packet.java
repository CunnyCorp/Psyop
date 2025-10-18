package monster.psyop.client.impl.modules.misc.rpc;

import com.google.gson.JsonObject;
import monster.psyop.client.impl.modules.misc.rpc.Opcode;

public record Packet(Opcode opcode, JsonObject data) {
}