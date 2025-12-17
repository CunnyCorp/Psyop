package monster.psyop.client.framework.modules.settings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import monster.psyop.client.utility.StringUtils;

import java.util.Iterator;
import java.util.function.Predicate;

public class GroupedSettings {
    public final String name;
    protected final ObjectArrayList<Setting<?, ?>> settings = new ObjectArrayList<>();
    private String description;
    @Getter
    @Setter
    private String label;
    @Setter
    private Predicate<GroupedSettings> visible = (group) -> true;

    public GroupedSettings(String name, String description) {
        this.name = name;
        this.label = StringUtils.readable(name);
        this.description = description;
    }

    public String description() {
        return description;
    }

    public String description(String v) {
        return description = v;
    }

    public <S> S add(S setting) {
        settings.add((Setting<S, ?>) setting);
        return setting;
    }

    public Iterator<Setting<?, ?>> get() {
        return settings.iterator();
    }

    public ObjectArrayList<Setting<?, ?>> getRaw() {
        return settings;
    }

    public boolean isVisible() {
        return visible.test(this);
    }
}
