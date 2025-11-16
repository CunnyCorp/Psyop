package monster.psyop.client.framework.modules.settings;

import imgui.ImGui;
import lombok.SneakyThrows;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.modules.settings.SettingConfig;
import monster.psyop.client.utility.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public class Setting<S, T> {
    public final transient String name;
    public final transient T defaultValue;
    public transient String description;
    public SettingConfig<?> settingConfig;
    private String label;
    private T value;
    private transient Predicate<S> visible = (setting) -> true;
    private final transient Predicate<S> onChanged = (setting) -> true;

    public Setting(SettingBuilder<?, ?, T> builder) {
        this.name = builder.name;
        if (builder.description != null) {
            this.description = builder.description;
        }
        this.defaultValue = builder.defaultValue;
        this.label =
                Objects.requireNonNullElseGet(builder.label, () -> StringUtils.readable(this.name));
        if (builder.visible != null) {
            this.visible = (Predicate<S>) builder.visible;
        }
        this.value = defaultValue;
    }

    public void onPopulated() {
    }

    public void resetToDefault() {
        this.value = this.defaultValue;
    }

    public boolean isVisible() {
        return visible.test((S) this);
    }

    public void render() {
        ImGui.bulletText(label() + " (" + this.getClass().getSimpleName() + ")");
    }

    public String label() {
        return StringUtils.readable(label);
    }

    public String label(String value) {
        this.label = value;
        return value;
    }

    public T value() {
        return value;
    }

    public T value(T value) {
        this.value = value;
        return value;
    }

    public void setGenericValue(Object value) {
        this.value = (T) value;
    }

    public boolean canReset() {
        return true;
    }

    public static class SettingBuilder<S, V, T> {
        private String name;
        private String description;
        private T defaultValue;
        private String label;
        private Predicate<S> visible;

        public V name(@NotNull String v) {
            this.name = v;
            return (V) this;
        }

        public V label(@NotNull String v) {
            this.label = v;
            return (V) this;
        }

        public V description(@NotNull String v) {
            this.description = v;
            return (V) this;
        }

        public V defaultTo(@NotNull T v) {
            this.defaultValue = v;
            return (V) this;
        }

        public V visible(@NotNull Predicate<S> v) {
            this.visible = v;
            return (V) this;
        }

        protected void check() {
            if (name == null) throw new RuntimeException("name is required when building a setting.");
            if (defaultValue == null)
                throw new RuntimeException("defaultValue is required when building a setting.");
        }

        @SneakyThrows
        public S build() {
            check();
            return (S) new Setting<>(this);
        }

        public S addTo(GroupedSettings groupedSettings) {
            S setting = this.build();
            groupedSettings.add(setting);
            return setting;
        }
    }
}
