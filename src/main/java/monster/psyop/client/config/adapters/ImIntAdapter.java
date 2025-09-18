package monster.psyop.client.config.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import imgui.type.ImInt;

import java.io.IOException;

public class ImIntAdapter extends TypeAdapter<ImInt> {
    @Override
    public void write(JsonWriter out, ImInt value) throws IOException {
        out.value((double) value.get());
    }

    @Override
    public ImInt read(JsonReader in) throws IOException {
        return new ImInt(((Double) in.nextDouble()).intValue());
    }
}
