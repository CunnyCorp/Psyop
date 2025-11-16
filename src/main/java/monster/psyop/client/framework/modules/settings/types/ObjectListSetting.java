package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImString;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.modules.settings.ListSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.utility.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class ObjectListSetting<S, T> extends Setting<S, ArrayList<T>> {
    public final Predicate<T> filter;
    private final ImString textFilter = new ImString();
    private boolean showSuggestions = false;

    public ObjectListSetting(Builder<S, T> builder) {
        super(builder);
        this.settingConfig = new ListSettingConfig();
        this.filter = builder.filter;
        textFilter.inputData.isResizable = true;
    }

    public abstract List<T> getSuggestions();

    @Override
    public void render() {
        if (ImGui.checkbox("Hide", Config.get().isHidden(this))) {
            Config.get().hide(this, !Config.get().isHidden(this));
        }

        if (Config.get().isHidden(this)) {
            return;
        }

        ImGui.beginChild(
                name + "_child", ImGui.getWindowWidth() - 10, ImGui.getWindowHeight() / 2, true);

        if (ImGui.beginTable(name + "_table", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.SizingFixedFit)) {
            T removeEntry = null;

            int i = 0;
            for (T entry : value()) {
                i++;
                if (textFilter.isEmpty()
                        || StringUtils.readable(itemToString(entry))
                        .toLowerCase()
                        .contains(textFilter.get().toLowerCase())) {
                    ImGui.tableNextColumn();

                    ImGui.text(StringUtils.readable(itemToString(entry)));

                    ImGui.tableNextColumn();
                    if (ImGui.button("Remove##" + i + "_" + name)) {
                        removeEntry = entry;
                    }
                }
            }

            if (removeEntry != null) {
                value().remove(removeEntry);
            }

            ImGui.endTable();
        }

        ImGui.endChild();

        ImGui.text("Filter:");
        ImGui.sameLine();
        float filterWidth = ImGui.getContentRegionAvail().x - 60;
        ImGui.setNextItemWidth(filterWidth);
        ImGui.inputTextWithHint("##filter_" + name, "Type to filter...", textFilter, ImGuiInputTextFlags.CallbackResize);

        ImGui.sameLine();
        if (ImGui.button("Clear##" + name)) {
            textFilter.set("");
        }

        ImGui.spacing();

        ImGui.text("Add new item:");

        if (getSuggestions() != null && !getSuggestions().isEmpty()) {
            if (ImGui.button("Show Suggestions##" + name)) {
                showSuggestions = !showSuggestions;
            }

            if (showSuggestions) {
                ImGui.beginChild(name + "_suggestions", 0, 150, true);

                boolean isFilterEmpty = textFilter.isEmpty();
                String lowerFilter = textFilter.get().toLowerCase();

                for (T value : getSuggestions()) {
                    if (value().contains(value)) {
                        continue;
                    }

                    String nameStr = StringUtils.readable(itemToString(value));

                    if (isFilterEmpty || nameStr.toLowerCase().contains(lowerFilter)) {
                        if (ImGui.selectable(nameStr)) {
                            synchronized (value()) {
                                value().add(value);
                            }
                        }

                        if (ImGui.isItemHovered()) {
                            ImGui.beginTooltip();
                            ImGui.text("Click to add: " + nameStr);
                            ImGui.endTooltip();
                        }
                    }
                }

                ImGui.endChild();
            }
        } else {
            ImGui.inputText("Item name", textFilter, ImGuiInputTextFlags.CallbackResize);
            ImGui.sameLine();
            if (ImGui.button("Add") && !textFilter.isEmpty()) {
                T newItem = parseItem(textFilter.get());
                if (newItem != null) {
                    synchronized (value()) {
                        value().add(newItem);
                    }
                    textFilter.set("");
                }
            }
        }
    }

    public abstract T parseItem(String v);

    public abstract String itemToString(T v);

    @Override
    public boolean canReset() {
        return false;
    }

    public static class Builder<S, T> extends SettingBuilder<S, Builder<S, T>, ArrayList<T>> {
        private Predicate<T> filter = (item) -> true;

        @Override
        public Builder<S, T> defaultTo(@NotNull ArrayList<T> v) {
            return super.defaultTo(new ArrayList<>(v));
        }

        public Builder<S, T> defaultTo(@NotNull List<T> v) {
            return super.defaultTo(new ArrayList<>(v));
        }

        public Builder<S, T> filter(Predicate<T> v) {
            this.filter = v;
            return this;
        }
    }
}