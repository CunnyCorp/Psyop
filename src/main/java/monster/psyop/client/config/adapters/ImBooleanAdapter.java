package monster.psyop.client.config.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import imgui.type.ImBoolean;

import java.io.IOException;

public class ImBooleanAdapter extends TypeAdapter<ImBoolean> {
    @Override
    public void write(JsonWriter out, ImBoolean value) throws IOException {
        out.value(value.get());
    }

    @Override
    public ImBoolean read(JsonReader in) throws IOException {
        return new ImBoolean(in.nextBoolean());
    }
}
