package monster.psyop.client.framework.gui.views.features.packetforming.fields;

import monster.psyop.client.framework.gui.views.features.packetforming.FieldWrap;

import java.lang.reflect.Field;

public class StringFieldWrap extends FieldWrap<String> {
    public StringFieldWrap(Field field, String obj, String name) {
        super(field, obj, name);
    }
}
