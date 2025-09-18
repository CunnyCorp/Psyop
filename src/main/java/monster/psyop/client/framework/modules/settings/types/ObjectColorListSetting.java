package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImString;
import monster.psyop.client.config.Config;
import monster.psyop.client.config.modules.settings.ListColorSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.utility.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class ObjectColorListSetting<S, T> extends Setting<S, ArrayList<T>> {
    public final Predicate<T> filter;
    protected String identifier = "owo";
    private final ImString textFilter = new ImString();
    public final Map<T, float[]> colorMap = new HashMap<>();
    private boolean showSuggestions = false;
    private final float childHeightRatio = 0.5f; // Configurable height ratio

    public ObjectColorListSetting(Builder<S, T> builder) {
        super(builder);
        this.settingConfig = new ListColorSettingConfig();
        this.filter = builder.filter;
        textFilter.inputData.isResizable = true;
    }

    public abstract List<T> getSuggestions();

    @Override
    public void render() {
        // Header with toggle
        ImGui.pushID(name);

        // Collapsible header
        if (ImGui.collapsingHeader(label() + "##" + name)) {
            ImGui.indent(10);

            // Visibility toggle
            boolean isHidden = Config.get().isHidden(this);
            if (ImGui.checkbox("Hide##" + name, isHidden)) {
                Config.get().hide(this, !isHidden);
            }

            if (!isHidden) {
                ImGui.spacing();

                // Filter input with clear button
                ImGui.text("Filter:");
                ImGui.sameLine();
                float filterWidth = ImGui.getContentRegionAvail().x - 60;
                ImGui.setNextItemWidth(filterWidth);
                String hint = getSuggestions().stream().findFirst().map(this::itemToString).orElse("");
                if (ImGui.inputTextWithHint("##filter_" + name, "Type to filter...", textFilter, ImGuiInputTextFlags.CallbackResize)) {
                    // Filter text changed
                }

                ImGui.sameLine();
                if (ImGui.button("Clear##" + name)) {
                    textFilter.set("");
                }

                ImGui.spacing();

                // Table with items
                float tableHeight = (ImGui.getWindowHeight() - ImGui.getCursorPosY() - 100) * childHeightRatio;
                if (ImGui.beginChild(name + "_table_child", 0, tableHeight, true, ImGuiWindowFlags.HorizontalScrollbar)) {
                    if (ImGui.beginTable(name + "_table", 4,
                            ImGuiTableFlags.Borders |
                                    ImGuiTableFlags.RowBg |
                                    ImGuiTableFlags.SizingStretchSame |
                                    ImGuiTableFlags.ScrollY)) {

                        // Table headers
                        ImGui.tableSetupColumn("Item", ImGuiTableColumnFlags.WidthStretch);
                        ImGui.tableSetupColumn("Color", ImGuiTableColumnFlags.WidthFixed, 100);
                        ImGui.tableSetupColumn("Actions", ImGuiTableColumnFlags.WidthFixed, 80);
                        ImGui.tableHeadersRow();

                        T removeEntry = null;
                        int i = 0;

                        for (T entry : value()) {
                            String entryStr = StringUtils.readable(itemToString(entry), Config.get().coreSettings);
                            String lowerFilter = textFilter.get().toLowerCase();

                            if (textFilter.isEmpty() || entryStr.toLowerCase().contains(lowerFilter)) {
                                ImGui.tableNextRow();
                                ImGui.tableNextColumn();

                                // Item name
                                ImGui.text(entryStr);

                                ImGui.tableNextColumn();

                                // Color editor
                                colorMap.putIfAbsent(entry, new float[]{0f, 0.4f, 0.2f, 1f});
                                ImGui.setNextItemWidth(-1);
                                ImGui.colorEdit4("##color_" + i + "_" + name, colorMap.get(entry),
                                        ImGuiColorEditFlags.NoInputs |
                                                ImGuiColorEditFlags.NoLabel |
                                                ImGuiColorEditFlags.AlphaPreview);

                                ImGui.tableNextColumn();

                                // Remove button
                                if (ImGui.button("Remove##" + i + "_" + name)) {
                                    removeEntry = entry;
                                }

                                i++;
                            }
                        }

                        if (removeEntry != null) {
                            value().remove(removeEntry);
                        }

                        ImGui.endTable();
                    }
                }
                ImGui.endChild();

                ImGui.spacing();

                // Suggestions section
                ImGui.text("Add new item:");

                if (getSuggestions() != null && !getSuggestions().isEmpty()) {
                    // Show suggestions dropdown
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

                            String nameStr = StringUtils.readable(itemToString(value), Config.get().coreSettings);

                            if (isFilterEmpty || nameStr.toLowerCase().contains(lowerFilter)) {
                                if (ImGui.selectable(nameStr)) {
                                    synchronized (value()) {
                                        value().add(value);
                                    }
                                }

                                // Tooltip with more info if available
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
                    // Manual input option
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

            ImGui.unindent(10);
        }

        ImGui.popID();
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