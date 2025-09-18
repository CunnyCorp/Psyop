package monster.psyop.client.framework.gui.views.features.packetforming;

import java.lang.reflect.Field;

public class FieldWrap<T> {
    public Field field;
    public T obj;
    public String name;

    public FieldWrap(Field field, T obj, String name) {
        this.field = field;
        this.obj = obj;
        this.name = name;
    }
}
