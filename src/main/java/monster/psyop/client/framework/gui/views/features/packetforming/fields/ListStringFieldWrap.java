package monster.psyop.client.framework.gui.views.features.packetforming.fields;

import monster.psyop.client.framework.gui.views.features.packetforming.FieldWrap;

import java.lang.reflect.Field;
import java.util.List;

public class ListStringFieldWrap extends FieldWrap<List<String>> {
    public ListStringFieldWrap(Field field, List<String> obj, String name) {
        super(field, obj, name);
    }
}
