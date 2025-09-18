package monster.psyop.client.framework.gui.views.features.packetforming.fields;

import monster.psyop.client.framework.gui.views.features.packetforming.FieldWrap;

import java.lang.reflect.Field;

public class IntFieldWrap extends FieldWrap<Integer> {
    public IntFieldWrap(Field field, Integer obj, String name) {
        super(field, obj, name);
    }
}
