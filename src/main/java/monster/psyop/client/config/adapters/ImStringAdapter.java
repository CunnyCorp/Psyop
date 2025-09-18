package monster.psyop.client.config.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import imgui.type.ImString;

import java.io.IOException;

public class ImStringAdapter extends TypeAdapter<ImString> {
    @Override
    public void write(JsonWriter out, ImString value) throws IOException {
        out.value(value.get());
    }

    @Override
    public ImString read(JsonReader in) throws IOException {
        return new ImString(in.nextString());
    }
}
